package com.xinchuang.diagnosis.ledger.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xinchuang.diagnosis.common.entity.BaseEntity;
import java.time.LocalDateTime;

@TableName("ledger_db_monitoring_config")
public class DbMonitoringConfig extends BaseEntity {
    private Long resourceId;
    private String prometheusBaseUrl;
    private String prometheusAuthRef;
    private String nodeTargetSelector;
    private String databaseTargetSelector;
    private String databaseType;
    private String status;
    private LocalDateTime lastCheckedAt;
    private String lastErrorMessage;

    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long value) { resourceId = value; }
    public String getPrometheusBaseUrl() { return prometheusBaseUrl; }
    public void setPrometheusBaseUrl(String value) { prometheusBaseUrl = value; }
    public String getPrometheusAuthRef() { return prometheusAuthRef; }
    public void setPrometheusAuthRef(String value) { prometheusAuthRef = value; }
    public String getNodeTargetSelector() { return nodeTargetSelector; }
    public void setNodeTargetSelector(String value) { nodeTargetSelector = value; }
    public String getDatabaseTargetSelector() { return databaseTargetSelector; }
    public void setDatabaseTargetSelector(String value) { databaseTargetSelector = value; }
    public String getDatabaseType() { return databaseType; }
    public void setDatabaseType(String value) { databaseType = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public LocalDateTime getLastCheckedAt() { return lastCheckedAt; }
    public void setLastCheckedAt(LocalDateTime value) { lastCheckedAt = value; }
    public String getLastErrorMessage() { return lastErrorMessage; }
    public void setLastErrorMessage(String value) { lastErrorMessage = value; }
}
