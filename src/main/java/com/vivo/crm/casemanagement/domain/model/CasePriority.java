package com.vivo.crm.casemanagement.domain.model;

/**
 * Prioridade do caso conforme TMF621
 */
public enum CasePriority {
    CRITICAL("Critical", "Crítica"),
    HIGH("High", "Alta"),
    MEDIUM("Medium", "Média"),
    LOW("Low", "Baixa");

    private final String tmfValue;
    private final String salesforceValue;

    CasePriority(String tmfValue, String salesforceValue) {
        this.tmfValue = tmfValue;
        this.salesforceValue = salesforceValue;
    }

    public String getTmfValue() {
        return tmfValue;
    }

    public String getSalesforceValue() {
        return salesforceValue;
    }

    public static CasePriority fromTmfValue(String tmfValue) {
        for (CasePriority priority : values()) {
            if (priority.tmfValue.equalsIgnoreCase(tmfValue)) {
                return priority;
            }
        }
        return MEDIUM;
    }
}
