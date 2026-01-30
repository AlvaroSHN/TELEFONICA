package com.vivo.crm.casemanagement.interfaces.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO para criação de TroubleTicket conforme TMF621
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TroubleTicketCreateRequest {

    @NotBlank(message = "O campo 'name' é obrigatório")
    private String name;

    @NotBlank(message = "O campo 'description' é obrigatório")
    private String description;

    private String ticketType;

    private String priority;

    private String severity;

    private ChannelRef channel;

    private List<RelatedPartyDto> relatedParty;

    private List<NoteDto> note;

    private List<TicketCharacteristicDto> ticketCharacteristic;

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
        private String text;
        private String author;
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
