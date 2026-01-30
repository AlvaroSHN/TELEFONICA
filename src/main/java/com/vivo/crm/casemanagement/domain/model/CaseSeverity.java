package com.vivo.crm.casemanagement.domain.model;

/**
 * Severidade do caso conforme TMF621
 */
public enum CaseSeverity {
    CRITICAL("Critical", "Critical"),
    MAJOR("Major", "Major"),
    MINOR("Minor", "Minor");

    private final String tmfValue;
    private final String salesforceValue;

    CaseSeverity(String tmfValue, String salesforceValue) {
        this.tmfValue = tmfValue;
        this.salesforceValue = salesforceValue;
    }

    public String getTmfValue() {
        return tmfValue;
    }

    public String getSalesforceValue() {
        return salesforceValue;
    }

    public static CaseSeverity fromTmfValue(String tmfValue) {
        for (CaseSeverity severity : values()) {
            if (severity.tmfValue.equalsIgnoreCase(tmfValue)) {
                return severity;
            }
        }
        return MINOR;
    }
}
