package com.vivo.crm.casemanagement.infrastructure.adapter.salesforce;

import com.vivo.crm.casemanagement.domain.model.Case;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Adapter para integração com Salesforce CRM
 * Implementa padrões de resiliência (Circuit Breaker, Retry)
 */
@Component
@Slf4j
public class SalesforceAdapter {

    private final WebClient webClient;
    private final String apiVersion;

    public SalesforceAdapter(
            @Value("${adapters.salesforce.base-url}") String baseUrl,
            @Value("${adapters.salesforce.api-version}") String apiVersion) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.apiVersion = apiVersion;
    }

    /**
     * Cria um Case no Salesforce
     */
    @CircuitBreaker(name = "salesforce", fallbackMethod = "createCaseFallback")
    @Retry(name = "salesforce")
    public Mono<SalesforceDto.CaseCreateResponse> createCase(Case caseEntity) {
        log.info("📤 Enviando caso para Salesforce: {}", caseEntity.getSubject());

        SalesforceDto.CaseCreateRequest request = mapToSalesforceRequest(caseEntity);

        return webClient.post()
                .uri("/services/data/{version}/sobjects/Case", apiVersion)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(SalesforceDto.CaseCreateResponse.class)
                .doOnSuccess(response -> log.info("✅ Caso criado no Salesforce: ID={}", response.getId()))
                .doOnError(error -> log.error("❌ Erro ao criar caso no Salesforce: {}", error.getMessage()));
    }

    /**
     * Busca um Case no Salesforce pelo ID
     */
    @CircuitBreaker(name = "salesforce", fallbackMethod = "getCaseFallback")
    public Mono<SalesforceDto.CaseResponse> getCase(String salesforceCaseId) {
        log.info("📥 Buscando caso no Salesforce: {}", salesforceCaseId);

        return webClient.get()
                .uri("/services/data/{version}/sobjects/Case/{id}", apiVersion, salesforceCaseId)
                .retrieve()
                .bodyToMono(SalesforceDto.CaseResponse.class)
                .doOnSuccess(response -> log.info("✅ Caso encontrado: CaseNumber={}", response.getCaseNumber()))
                .doOnError(error -> log.error("❌ Erro ao buscar caso no Salesforce: {}", error.getMessage()));
    }

    /**
     * Atualiza um Case no Salesforce
     */
    @CircuitBreaker(name = "salesforce", fallbackMethod = "updateCaseFallback")
    @Retry(name = "salesforce")
    public Mono<Void> updateCase(String salesforceCaseId, Case caseEntity) {
        log.info("📤 Atualizando caso no Salesforce: {}", salesforceCaseId);

        SalesforceDto.CaseUpdateRequest request = SalesforceDto.CaseUpdateRequest.builder()
                .subject(caseEntity.getSubject())
                .description(caseEntity.getDescription())
                .status(caseEntity.getStatus() != null ? caseEntity.getStatus().getSalesforceValue() : null)
                .priority(caseEntity.getPriority() != null ? caseEntity.getPriority().getSalesforceValue() : null)
                .resolution(caseEntity.getResolution())
                .build();

        return webClient.patch()
                .uri("/services/data/{version}/sobjects/Case/{id}", apiVersion, salesforceCaseId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.info("✅ Caso atualizado no Salesforce"))
                .doOnError(error -> log.error("❌ Erro ao atualizar caso no Salesforce: {}", error.getMessage()));
    }

    /**
     * Mapeia entidade Case para request do Salesforce
     */
    private SalesforceDto.CaseCreateRequest mapToSalesforceRequest(Case caseEntity) {
        return SalesforceDto.CaseCreateRequest.builder()
                .subject(caseEntity.getSubject())
                .description(caseEntity.getDescription())
                .status(caseEntity.getStatus() != null ? caseEntity.getStatus().getSalesforceValue() : "Novo")
                .priority(caseEntity.getPriority() != null ? caseEntity.getPriority().getSalesforceValue() : "Média")
                .type(caseEntity.getTicketType())
                .origin(caseEntity.getChannelName())
                .severity(caseEntity.getSeverity() != null ? caseEntity.getSeverity().getSalesforceValue() : null)
                .build();
    }

    // === Fallback Methods ===

    private Mono<SalesforceDto.CaseCreateResponse> createCaseFallback(Case caseEntity, Throwable t) {
        log.warn("⚠️ Fallback ativado para criação de caso. Erro: {}", t.getMessage());
        // Retorna um response simulado para não bloquear o fluxo
        return Mono.just(SalesforceDto.CaseCreateResponse.builder()
                .id("FALLBACK-" + System.currentTimeMillis())
                .success(false)
                .errors(new Object[]{"Salesforce indisponível - caso será sincronizado posteriormente"})
                .build());
    }

    private Mono<SalesforceDto.CaseResponse> getCaseFallback(String salesforceCaseId, Throwable t) {
        log.warn("⚠️ Fallback ativado para busca de caso. Erro: {}", t.getMessage());
        return Mono.empty();
    }

    private Mono<Void> updateCaseFallback(String salesforceCaseId, Case caseEntity, Throwable t) {
        log.warn("⚠️ Fallback ativado para atualização de caso. Erro: {}", t.getMessage());
        return Mono.empty();
    }
}
