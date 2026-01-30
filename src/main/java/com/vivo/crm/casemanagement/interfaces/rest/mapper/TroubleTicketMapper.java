package com.vivo.crm.casemanagement.interfaces.rest.mapper;

import com.vivo.crm.casemanagement.domain.model.*;
import com.vivo.crm.casemanagement.interfaces.rest.dto.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mapper para conversão entre DTOs TMF621 e entidades de domínio
 */
@Component
public class TroubleTicketMapper {

    private static final String BASE_URL = "/tmf-api/troubleTicket/v4/troubleTicket/";

    /**
     * Converte requisição de criação TMF621 para entidade Case
     */
    public Case toEntity(TroubleTicketCreateRequest request) {
        Case caseEntity = Case.builder()
                .subject(request.getName())
                .description(request.getDescription())
                .ticketType(request.getTicketType())
                .priority(request.getPriority() != null ? 
                         CasePriority.fromTmfValue(request.getPriority()) : CasePriority.MEDIUM)
                .severity(request.getSeverity() != null ? 
                         CaseSeverity.fromTmfValue(request.getSeverity()) : null)
                .status(CaseStatus.NEW)
                .ticketCharacteristics(new HashMap<>())
                .build();

        // Mapear canal
        if (request.getChannel() != null) {
            caseEntity.setChannel(request.getChannel().getId());
            caseEntity.setChannelName(request.getChannel().getName());
        }

        // Mapear características customizadas
        if (request.getTicketCharacteristic() != null) {
            Map<String, String> characteristics = new HashMap<>();
            for (TroubleTicketCreateRequest.TicketCharacteristicDto tc : request.getTicketCharacteristic()) {
                characteristics.put(tc.getName(), tc.getValue());
            }
            caseEntity.setTicketCharacteristics(characteristics);
        }

        return caseEntity;
    }

    /**
     * Converte entidade Case para resposta TMF621
     */
    public TroubleTicketResponse toResponse(Case caseEntity) {
        TroubleTicketResponse response = TroubleTicketResponse.builder()
                .id(caseEntity.getProtocol())
                .href(BASE_URL + caseEntity.getProtocol())
                .name(caseEntity.getSubject())
                .description(caseEntity.getDescription())
                .ticketType(caseEntity.getTicketType())
                .priority(caseEntity.getPriority() != null ? caseEntity.getPriority().getTmfValue() : null)
                .severity(caseEntity.getSeverity() != null ? caseEntity.getSeverity().getTmfValue() : null)
                .status(caseEntity.getStatus() != null ? caseEntity.getStatus().getTmfValue() : null)
                .creationDate(caseEntity.getCreatedAt())
                .lastUpdate(caseEntity.getUpdatedAt())
                .resolutionDate(caseEntity.getResolvedAt())
                .salesforceCaseId(caseEntity.getSalesforceCaseId())
                .salesforceCaseNumber(caseEntity.getSalesforceCaseNumber())
                .protocol(caseEntity.getProtocol())
                .build();

        // Mapear canal
        if (caseEntity.getChannel() != null) {
            response.setChannel(TroubleTicketResponse.ChannelRef.builder()
                    .id(caseEntity.getChannel())
                    .name(caseEntity.getChannelName())
                    .build());
        }

        // Mapear notas
        if (caseEntity.getNotes() != null && !caseEntity.getNotes().isEmpty()) {
            List<TroubleTicketResponse.NoteDto> notes = caseEntity.getNotes().stream()
                    .map(note -> TroubleTicketResponse.NoteDto.builder()
                            .id(note.getNoteId())
                            .text(note.getText())
                            .author(note.getAuthor())
                            .date(note.getCreatedAt())
                            .build())
                    .collect(Collectors.toList());
            response.setNote(notes);
        }

        // Mapear partes relacionadas
        if (caseEntity.getRelatedParties() != null && !caseEntity.getRelatedParties().isEmpty()) {
            List<TroubleTicketResponse.RelatedPartyDto> parties = caseEntity.getRelatedParties().stream()
                    .map(party -> TroubleTicketResponse.RelatedPartyDto.builder()
                            .referredType(party.getReferredType())
                            .id(party.getPartyId())
                            .name(party.getName())
                            .role(party.getRole())
                            .build())
                    .collect(Collectors.toList());
            response.setRelatedParty(parties);
        }

        // Mapear características customizadas
        if (caseEntity.getTicketCharacteristics() != null && !caseEntity.getTicketCharacteristics().isEmpty()) {
            List<TroubleTicketResponse.TicketCharacteristicDto> characteristics = 
                    caseEntity.getTicketCharacteristics().entrySet().stream()
                    .map(entry -> TroubleTicketResponse.TicketCharacteristicDto.builder()
                            .name(entry.getKey())
                            .value(entry.getValue())
                            .build())
                    .collect(Collectors.toList());
            response.setTicketCharacteristic(characteristics);
        }

        return response;
    }

    /**
     * Aplica atualização na entidade Case
     */
    public void applyUpdate(Case caseEntity, TroubleTicketUpdateRequest request) {
        if (request.getName() != null) {
            caseEntity.setSubject(request.getName());
        }
        if (request.getDescription() != null) {
            caseEntity.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            caseEntity.setStatus(CaseStatus.fromTmfValue(request.getStatus()));
        }
        if (request.getPriority() != null) {
            caseEntity.setPriority(CasePriority.fromTmfValue(request.getPriority()));
        }
        if (request.getSeverity() != null) {
            caseEntity.setSeverity(CaseSeverity.fromTmfValue(request.getSeverity()));
        }
        if (request.getResolution() != null) {
            caseEntity.setResolution(request.getResolution());
        }
    }
}
