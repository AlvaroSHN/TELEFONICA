package com.vivo.crm.casemanagement.interfaces.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para atualização de TroubleTicket conforme TMF621
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TroubleTicketUpdateRequest {

    private String name;

    private String description;

    private String status;

    private String priority;

    private String severity;

    private String resolution;

    private List<NoteDto> note;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoteDto {
        private String text;
        private String author;
    }
}
