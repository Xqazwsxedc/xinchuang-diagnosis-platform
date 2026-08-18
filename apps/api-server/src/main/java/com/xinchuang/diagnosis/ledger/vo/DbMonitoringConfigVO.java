package com.xinchuang.diagnosis.ledger.vo;

import java.time.LocalDateTime;
import java.util.Map;

public class DbMonitoringConfigVO {
    private String prometheusBaseUrl;
    private boolean credentialsConfigured;
    private Map<String, String> nodeTargetSelector;
    private Map<String, String> databaseTargetSelector;
    private String databaseType;
    private String status;
    private LocalDateTime lastCheckedAt;
    private String lastErrorMessage;

    public String getPrometheusBaseUrl() { return prometheusBaseUrl; }
    public void setPrometheusBaseUrl(String value) { prometheusBaseUrl = value; }
    public boolean isCredentialsConfigured() { return credentialsConfigured; }
    public void setCredentialsConfigured(boolean value) { credentialsConfigured = value; }
    public Map<String, String> getNodeTargetSelector() { return nodeTargetSelector; }
    public void setNodeTargetSelector(Map<String, String> value) { nodeTargetSelector = value; }
    public Map<String, String> getDatabaseTargetSelector() { return databaseTargetSelector; }
    public void setDatabaseTargetSelector(Map<String, String> value) { databaseTargetSelector = value; }
    public String getDatabaseType() { return databaseType; }
    public void setDatabaseType(String value) { databaseType = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public LocalDateTime getLastCheckedAt() { return lastCheckedAt; }
    public void setLastCheckedAt(LocalDateTime value) { lastCheckedAt = value; }
    public String getLastErrorMessage() { return lastErrorMessage; }
    public void setLastErrorMessage(String value) { lastErrorMessage = value; }
}
