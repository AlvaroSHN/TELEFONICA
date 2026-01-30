package com.vivo.crm.casemanagement.application.service;

import com.vivo.crm.casemanagement.domain.model.*;
import com.vivo.crm.casemanagement.domain.repository.CaseRepository;
import com.vivo.crm.casemanagement.infrastructure.adapter.salesforce.SalesforceAdapter;
import com.vivo.crm.casemanagement.infrastructure.adapter.salesforce.SalesforceDto;
import com.vivo.crm.casemanagement.interfaces.rest.dto.*;
import com.vivo.crm.casemanagement.interfaces.rest.mapper.TroubleTicketMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço de aplicação para gerenciamento de casos
 * Orquestra o fluxo entre a API TMF621 e o Salesforce
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CaseService {

    private final CaseRepository caseRepository;
    private final SalesforceAdapter salesforceAdapter;
    private final TroubleTicketMapper mapper;

    /**
     * Cria um novo caso
     * Fluxo: Recebe TMF621 -> Salva local -> Envia para Salesforce -> Atualiza com ID do SF
     */
    @Transactional
    public Mono<TroubleTicketResponse> createCase(TroubleTicketCreateRequest request) {
        log.info("🎫 Iniciando criação de caso: {}", request.getName());

        // 1. Converter para entidade de domínio
        Case caseEntity = mapper.toEntity(request);

        // 2. Adicionar partes relacionadas
        if (request.getRelatedParty() != null) {
            for (TroubleTicketCreateRequest.RelatedPartyDto partyDto : request.getRelatedParty()) {
                RelatedParty party = RelatedParty.builder()
                        .referredType(partyDto.getReferredType())
                        .partyId(partyDto.getId())
                        .name(partyDto.getName())
                        .role(partyDto.getRole())
                        .build();
                caseEntity.addRelatedParty(party);

                // Extrair customerId do Contact
                if ("Contact".equalsIgnoreCase(partyDto.getReferredType())) {
                    caseEntity.setCustomerId(partyDto.getId());
                    caseEntity.setCustomerName(partyDto.getName());
                }
            }
        }

        // 3. Adicionar notas iniciais
        if (request.getNote() != null) {
            for (TroubleTicketCreateRequest.NoteDto noteDto : request.getNote()) {
                CaseNote note = CaseNote.builder()
                        .text(noteDto.getText())
                        .author(noteDto.getAuthor())
                        .build();
                caseEntity.addNote(note);
            }
        }

        // 4. Salvar localmente primeiro
        Case savedCase = caseRepository.save(caseEntity);
        log.info("💾 Caso salvo localmente: protocol={}", savedCase.getProtocol());

        // 5. Enviar para Salesforce de forma assíncrona
        return salesforceAdapter.createCase(savedCase)
                .map(sfResponse -> {
                    // 6. Atualizar com dados do Salesforce
                    savedCase.setSalesforceCaseId(sfResponse.getId());
                    if (sfResponse.isSuccess()) {
                        log.info("✅ Caso sincronizado com Salesforce: sfId={}", sfResponse.getId());
                    } else {
                        log.warn("⚠️ Caso criado localmente, mas houve problema no Salesforce");
                    }
                    caseRepository.save(savedCase);
                    return mapper.toResponse(savedCase);
                })
                .onErrorResume(error -> {
                    log.error("❌ Erro na integração com Salesforce: {}", error.getMessage());
                    // Retorna o caso mesmo sem sincronização com SF
                    return Mono.just(mapper.toResponse(savedCase));
                });
    }

    /**
     * Busca um caso pelo ID (protocol)
     */
    public Mono<TroubleTicketResponse> getCaseById(String id) {
        log.info("🔍 Buscando caso: {}", id);

        return Mono.justOrEmpty(caseRepository.findByProtocol(id))
                .map(mapper::toResponse)
                .switchIfEmpty(Mono.error(new CaseNotFoundException("Caso não encontrado: " + id)));
    }

    /**
     * Lista casos com filtros opcionais
     */
    public Mono<List<TroubleTicketResponse>> listCases(String status, String priority, String ticketType) {
        log.info("📋 Listando casos - status={}, priority={}, ticketType={}", status, priority, ticketType);

        CaseStatus caseStatus = status != null ? CaseStatus.fromTmfValue(status) : null;
        CasePriority casePriority = priority != null ? CasePriority.fromTmfValue(priority) : null;

        List<Case> cases = caseRepository.findByFilters(caseStatus, casePriority, ticketType);

        List<TroubleTicketResponse> responses = cases.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        return Mono.just(responses);
    }

    /**
     * Atualiza um caso existente
     */
    @Transactional
    public Mono<TroubleTicketResponse> updateCase(String id, TroubleTicketUpdateRequest request) {
        log.info("📝 Atualizando caso: {}", id);

        Case caseEntity = caseRepository.findByProtocol(id)
                .orElseThrow(() -> new CaseNotFoundException("Caso não encontrado: " + id));

        // Aplicar atualizações
        mapper.applyUpdate(caseEntity, request);

        // Adicionar novas notas
        if (request.getNote() != null) {
            for (TroubleTicketUpdateRequest.NoteDto noteDto : request.getNote()) {
                CaseNote note = CaseNote.builder()
                        .text(noteDto.getText())
                        .author(noteDto.getAuthor())
                        .build();
                caseEntity.addNote(note);
            }
        }

        // Verificar se foi resolvido
        if (CaseStatus.RESOLVED.equals(caseEntity.getStatus()) && caseEntity.getResolvedAt() == null) {
            caseEntity.setResolvedAt(Instant.now());
        }

        Case updatedCase = caseRepository.save(caseEntity);

        // Sincronizar com Salesforce se tiver ID
        if (updatedCase.getSalesforceCaseId() != null) {
            return salesforceAdapter.updateCase(updatedCase.getSalesforceCaseId(), updatedCase)
                    .thenReturn(mapper.toResponse(updatedCase))
                    .onErrorResume(error -> {
                        log.warn("⚠️ Erro ao sincronizar atualização com Salesforce: {}", error.getMessage());
                        return Mono.just(mapper.toResponse(updatedCase));
                    });
        }

        return Mono.just(mapper.toResponse(updatedCase));
    }

    /**
     * Deleta um caso (soft delete - muda status para CANCELLED)
     */
    @Transactional
    public Mono<Void> deleteCase(String id) {
        log.info("🗑️ Cancelando caso: {}", id);

        Case caseEntity = caseRepository.findByProtocol(id)
                .orElseThrow(() -> new CaseNotFoundException("Caso não encontrado: " + id));

        caseEntity.setStatus(CaseStatus.CANCELLED);
        caseRepository.save(caseEntity);

        return Mono.empty();
    }

    /**
     * Exception para caso não encontrado
     */
    public static class CaseNotFoundException extends RuntimeException {
        public CaseNotFoundException(String message) {
            super(message);
        }
    }
}
