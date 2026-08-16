package com.xinchuang.diagnosis.ai.vo;

import com.xinchuang.diagnosis.ai.entity.FaultReport;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FaultAlertVO {
    private String id;
    private String reportCode;
    private String targetType;
    private String targetId;
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
    private String handledBy;
    private LocalDateTime handledAt;

    public static FaultAlertVO from(FaultReport report) {
        FaultAlertVO vo = new FaultAlertVO();
        vo.populate(report);
        return vo;
    }

    protected void populate(FaultReport report) {
        id = report.getId() == null ? null : report.getId().toString();
        reportCode = report.getReportCode();
        targetType = report.getTargetType();
        targetId = report.getTargetId() == null ? null : report.getTargetId().toString();
        targetNameSnapshot = report.getTargetNameSnapshot();
        alertTime = report.getAlertTime();
        indicatorCategory = report.getIndicatorCategory();
        faultType = report.getFaultType();
        riskLevel = report.getRiskLevel();
        expectedStartTime = report.getExpectedStartTime();
        expectedEndTime = report.getExpectedEndTime();
        confidence = report.getConfidence();
        forecastDescription = report.getForecastDescription();
        handleSuggestion = report.getHandleSuggestion();
        trendJson = report.getTrendJson();
        processStatus = report.getProcessStatus();
        handledBy = report.getHandledBy() == null ? null : report.getHandledBy().toString();
        handledAt = report.getHandledAt();
    }

    public String getId() { return id; }
    public String getReportCode() { return reportCode; }
    public String getTargetType() { return targetType; }
    public String getTargetId() { return targetId; }
    public String getTargetNameSnapshot() { return targetNameSnapshot; }
    public LocalDateTime getAlertTime() { return alertTime; }
    public String getIndicatorCategory() { return indicatorCategory; }
    public String getFaultType() { return faultType; }
    public String getRiskLevel() { return riskLevel; }
    public LocalDateTime getExpectedStartTime() { return expectedStartTime; }
    public LocalDateTime getExpectedEndTime() { return expectedEndTime; }
    public BigDecimal getConfidence() { return confidence; }
    public String getForecastDescription() { return forecastDescription; }
    public String getHandleSuggestion() { return handleSuggestion; }
    public String getTrendJson() { return trendJson; }
    public String getProcessStatus() { return processStatus; }
    public String getHandledBy() { return handledBy; }
    public LocalDateTime getHandledAt() { return handledAt; }
}
