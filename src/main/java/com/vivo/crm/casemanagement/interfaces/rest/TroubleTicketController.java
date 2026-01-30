package com.vivo.crm.casemanagement.interfaces.rest;

import com.vivo.crm.casemanagement.application.service.CaseService;
import com.vivo.crm.casemanagement.interfaces.rest.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Controller REST para API TMF621 - Trouble Ticket
 * Expõe endpoints para gerenciamento de tickets de atendimento
 */
@RestController
@RequestMapping("/tmf-api/troubleTicket/v4")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "TroubleTicket", description = "TMF621 - Trouble Ticket Management API")
public class TroubleTicketController {

    private final CaseService caseService;

    /**
     * Cria um novo TroubleTicket
     */
    @PostMapping(value = "/troubleTicket", 
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Criar TroubleTicket", 
               description = "Cria um novo ticket de atendimento e sincroniza com Salesforce")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Ticket criado com sucesso",
                     content = @Content(schema = @Schema(implementation = TroubleTicketResponse.class))),
        @ApiResponse(responseCode = "400", description = "Requisição inválida"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public Mono<ResponseEntity<TroubleTicketResponse>> createTroubleTicket(
            @Valid @RequestBody TroubleTicketCreateRequest request) {
        
        log.info("📨 POST /troubleTicket - Criando ticket: {}", request.getName());
        
        return caseService.createCase(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .doOnSuccess(r -> log.info("✅ Ticket criado: {}", r.getBody().getId()));
    }

    /**
     * Busca um TroubleTicket pelo ID
     */
    @GetMapping(value = "/troubleTicket/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Buscar TroubleTicket", 
               description = "Retorna os detalhes de um ticket específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ticket encontrado",
                     content = @Content(schema = @Schema(implementation = TroubleTicketResponse.class))),
        @ApiResponse(responseCode = "404", description = "Ticket não encontrado")
    })
    public Mono<ResponseEntity<TroubleTicketResponse>> getTroubleTicket(
            @Parameter(description = "ID do ticket (protocol)") @PathVariable String id) {
        
        log.info("📨 GET /troubleTicket/{}", id);
        
        return caseService.getCaseById(id)
                .map(ResponseEntity::ok)
                .onErrorResume(CaseService.CaseNotFoundException.class, 
                              e -> Mono.just(ResponseEntity.notFound().build()));
    }

    /**
     * Lista TroubleTickets com filtros opcionais
     */
    @GetMapping(value = "/troubleTicket", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Listar TroubleTickets", 
               description = "Lista tickets com filtros opcionais por status, prioridade e tipo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de tickets")
    })
    public Mono<ResponseEntity<List<TroubleTicketResponse>>> listTroubleTickets(
            @Parameter(description = "Filtrar por status (new, inProgress, resolved, etc)")
            @RequestParam(required = false) String status,
            
            @Parameter(description = "Filtrar por prioridade (Critical, High, Medium, Low)")
            @RequestParam(required = false) String priority,
            
            @Parameter(description = "Filtrar por tipo de ticket")
            @RequestParam(required = false) String ticketType,
            
            @Parameter(description = "Limite de resultados")
            @RequestParam(required = false, defaultValue = "100") Integer limit) {
        
        log.info("📨 GET /troubleTicket - status={}, priority={}, ticketType={}", status, priority, ticketType);
        
        return caseService.listCases(status, priority, ticketType)
                .map(ResponseEntity::ok);
    }

    /**
     * Atualiza um TroubleTicket existente
     */
    @PatchMapping(value = "/troubleTicket/{id}",
                  consumes = MediaType.APPLICATION_JSON_VALUE,
                  produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualizar TroubleTicket", 
               description = "Atualiza parcialmente um ticket existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ticket atualizado",
                     content = @Content(schema = @Schema(implementation = TroubleTicketResponse.class))),
        @ApiResponse(responseCode = "404", description = "Ticket não encontrado")
    })
    public Mono<ResponseEntity<TroubleTicketResponse>> updateTroubleTicket(
            @Parameter(description = "ID do ticket (protocol)") @PathVariable String id,
            @RequestBody TroubleTicketUpdateRequest request) {
        
        log.info("📨 PATCH /troubleTicket/{}", id);
        
        return caseService.updateCase(id, request)
                .map(ResponseEntity::ok)
                .onErrorResume(CaseService.CaseNotFoundException.class,
                              e -> Mono.just(ResponseEntity.notFound().build()));
    }

    /**
     * Deleta (cancela) um TroubleTicket
     */
    @DeleteMapping(value = "/troubleTicket/{id}")
    @Operation(summary = "Deletar TroubleTicket", 
               description = "Cancela um ticket existente (soft delete)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Ticket cancelado"),
        @ApiResponse(responseCode = "404", description = "Ticket não encontrado")
    })
    public Mono<ResponseEntity<Void>> deleteTroubleTicket(
            @Parameter(description = "ID do ticket (protocol)") @PathVariable String id) {
        
        log.info("📨 DELETE /troubleTicket/{}", id);
        
        return caseService.deleteCase(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(CaseService.CaseNotFoundException.class,
                              e -> Mono.just(ResponseEntity.notFound().build()));
    }
}
