package com.xinchuang.diagnosis.ledger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public class DbMonitoringConfigRequest {
    @NotBlank
    private String prometheusBaseUrl;
    private String prometheusAuthRef;
    @NotNull
    private Map<String, String> nodeTargetSelector;
    @NotNull
    private Map<String, String> databaseTargetSelector;
    @NotBlank
    private String databaseType;

    public String getPrometheusBaseUrl() { return prometheusBaseUrl; }
    public void setPrometheusBaseUrl(String value) { prometheusBaseUrl = value; }
    public String getPrometheusAuthRef() { return prometheusAuthRef; }
    public void setPrometheusAuthRef(String value) { prometheusAuthRef = value; }
    public Map<String, String> getNodeTargetSelector() { return nodeTargetSelector; }
    public void setNodeTargetSelector(Map<String, String> value) { nodeTargetSelector = value; }
    public Map<String, String> getDatabaseTargetSelector() { return databaseTargetSelector; }
    public void setDatabaseTargetSelector(Map<String, String> value) { databaseTargetSelector = value; }
    public String getDatabaseType() { return databaseType; }
    public void setDatabaseType(String value) { databaseType = value; }
}
