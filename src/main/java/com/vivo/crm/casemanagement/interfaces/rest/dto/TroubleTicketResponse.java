package com.vivo.crm.casemanagement.interfaces.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * DTO de resposta TroubleTicket conforme TMF621
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TroubleTicketResponse {

    private String id;

    private String href;

    private String name;

    private String description;

    private String ticketType;

    private String priority;

    private String severity;

    private String status;

    private ChannelRef channel;

    private List<RelatedPartyDto> relatedParty;

    private List<NoteDto> note;

    private List<TicketCharacteristicDto> ticketCharacteristic;

    private Instant creationDate;

    private Instant lastUpdate;

    private Instant resolutionDate;

    // Campos adicionais para rastreabilidade
    private String salesforceCaseId;

    private String salesforceCaseNumber;

    private String protocol;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChannelRef {
        private String id;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelatedPartyDto {
        @JsonProperty("@referredType")
        private String referredType;
        private String id;
        private String name;
        private String role;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoteDto {
        private String id;
        private String text;
        private String author;
        private Instant date;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketCharacteristicDto {
        private String name;
        private String value;
    }
}
