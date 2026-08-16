package com.xinchuang.diagnosis.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xinchuang.diagnosis.common.entity.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("fault_report")
public class FaultReport extends BaseEntity {
    private String reportCode;
    private Long taskId;
    private Long executionId;
    private String targetType;
    private Long targetId;
    private String targetNameSnapshot;
    private LocalDateTime alertTime;
    private String indicatorCategory;
    private String faultType;
    private String riskLevel;
    private LocalDateTime expectedStartTime;
    private LocalDateTime expectedEndTime;
    private BigDecimal confidence;
    private String forecastDescription;
    private String handleSuggestion;
    private String trendJson;
    private String processStatus;
    private Long handledBy;
    private LocalDateTime handledAt;

    public String getReportCode() { return reportCode; }
    public void setReportCode(String reportCode) { this.reportCode = reportCode; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public Long getExecutionId() { return executionId; }
    public void setExecutionId(Long executionId) { this.executionId = executionId; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public String getTargetNameSnapshot() { return targetNameSnapshot; }
    public void setTargetNameSnapshot(String targetNameSnapshot) { this.targetNameSnapshot = targetNameSnapshot; }
    public LocalDateTime getAlertTime() { return alertTime; }
    public void setAlertTime(LocalDateTime alertTime) { this.alertTime = alertTime; }
    public String getIndicatorCategory() { return indicatorCategory; }
    public void setIndicatorCategory(String indicatorCategory) { this.indicatorCategory = indicatorCategory; }
    public String getFaultType() { return faultType; }
    public void setFaultType(String faultType) { this.faultType = faultType; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public LocalDateTime getExpectedStartTime() { return expectedStartTime; }
    public void setExpectedStartTime(LocalDateTime expectedStartTime) { this.expectedStartTime = expectedStartTime; }
    public LocalDateTime getExpectedEndTime() { return expectedEndTime; }
    public void setExpectedEndTime(LocalDateTime expectedEndTime) { this.expectedEndTime = expectedEndTime; }
    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }
    public String getForecastDescription() { return forecastDescription; }
    public void setForecastDescription(String forecastDescription) { this.forecastDescription = forecastDescription; }
    public String getHandleSuggestion() { return handleSuggestion; }
    public void setHandleSuggestion(String handleSuggestion) { this.handleSuggestion = handleSuggestion; }
    public String getTrendJson() { return trendJson; }
    public void setTrendJson(String trendJson) { this.trendJson = trendJson; }
    public String getProcessStatus() { return processStatus; }
    public void setProcessStatus(String processStatus) { this.processStatus = processStatus; }
    public Long getHandledBy() { return handledBy; }
    public void setHandledBy(Long handledBy) { this.handledBy = handledBy; }
    public LocalDateTime getHandledAt() { return handledAt; }
    public void setHandledAt(LocalDateTime handledAt) { this.handledAt = handledAt; }
}
