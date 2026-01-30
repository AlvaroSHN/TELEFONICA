package com.vivo.crm.casemanagement.infrastructure.adapter.salesforce;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTOs para comunicação com API do Salesforce
 */
public class SalesforceDto {

    /**
     * Request para criar Case no Salesforce
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CaseCreateRequest {
        @JsonProperty("Subject")
        private String subject;

        @JsonProperty("Description")
        private String description;

        @JsonProperty("Status")
        private String status;

        @JsonProperty("Priority")
        private String priority;

        @JsonProperty("Type")
        private String type;

        @JsonProperty("Origin")
        private String origin;

        @JsonProperty("AccountId")
        private String accountId;

        @JsonProperty("ContactId")
        private String contactId;

        @JsonProperty("vlocity_cmt__severity__c")
        private String severity;
    }

    /**
     * Response da criação de Case no Salesforce
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CaseCreateResponse {
        private String id;
        private boolean success;
        private Object[] errors;
    }

    /**
     * Response de busca de Case no Salesforce
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CaseResponse {
        @JsonProperty("Id")
        private String id;

        @JsonProperty("CaseNumber")
        private String caseNumber;

        @JsonProperty("Subject")
        private String subject;

        @JsonProperty("Description")
        private String description;

        @JsonProperty("Status")
        private String status;

        @JsonProperty("Priority")
        private String priority;

        @JsonProperty("Type")
        private String type;

        @JsonProperty("Origin")
        private String origin;

        @JsonProperty("CreatedDate")
        private String createdDate;

        @JsonProperty("LastModifiedDate")
        private String lastModifiedDate;

        @JsonProperty("ClosedDate")
        private String closedDate;
    }

    /**
     * Request para atualizar Case no Salesforce
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CaseUpdateRequest {
        @JsonProperty("Subject")
        private String subject;

        @JsonProperty("Description")
        private String description;

        @JsonProperty("Status")
        private String status;

        @JsonProperty("Priority")
        private String priority;

        @JsonProperty("Resolution__c")
        private String resolution;
    }
}
