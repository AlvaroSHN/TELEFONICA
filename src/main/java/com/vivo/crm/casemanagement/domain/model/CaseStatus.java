package com.vivo.crm.casemanagement.domain.model;

/**
 * Status do caso conforme TMF621
 */
public enum CaseStatus {
    NEW("new", "Novo"),
    ACKNOWLEDGED("acknowledged", "Em Análise"),
    IN_PROGRESS("inProgress", "Em Andamento"),
    PENDING("pending", "Pendente"),
    HELD("held", "Em Espera"),
    RESOLVED("resolved", "Resolvido"),
    CLOSED("closed", "Fechado"),
    CANCELLED("cancelled", "Cancelado");

    private final String tmfValue;
    private final String salesforceValue;

    CaseStatus(String tmfValue, String salesforceValue) {
        this.tmfValue = tmfValue;
        this.salesforceValue = salesforceValue;
    }

    public String getTmfValue() {
        return tmfValue;
    }

    public String getSalesforceValue() {
        return salesforceValue;
    }

    public static CaseStatus fromTmfValue(String tmfValue) {
        for (CaseStatus status : values()) {
            if (status.tmfValue.equalsIgnoreCase(tmfValue)) {
                return status;
            }
        }
        return NEW;
    }
}
