package com.xinchuang.diagnosis.ai.vo;

import java.time.LocalDateTime;

public class FaultAlertHistoryVO {
    private String beforeStatus;
    private String afterStatus;
    private String operationRemark;
    private String operatorName;
    private LocalDateTime createdAt;

    public String getBeforeStatus() { return beforeStatus; }
    public void setBeforeStatus(String beforeStatus) { this.beforeStatus = beforeStatus; }
    public String getAfterStatus() { return afterStatus; }
    public void setAfterStatus(String afterStatus) { this.afterStatus = afterStatus; }
    public String getOperationRemark() { return operationRemark; }
    public void setOperationRemark(String operationRemark) { this.operationRemark = operationRemark; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
