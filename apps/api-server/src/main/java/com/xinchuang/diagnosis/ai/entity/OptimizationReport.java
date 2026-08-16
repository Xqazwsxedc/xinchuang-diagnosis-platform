package com.xinchuang.diagnosis.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.xinchuang.diagnosis.common.entity.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("opt_report")
public class OptimizationReport extends BaseEntity {
    private String reportCode;
    private Long taskId;
    private Long executionId;
    private String taskNameSnapshot;
    private String targetType;
    private Long targetId;
    private String targetNameSnapshot;
    private String optimizationType;
    private String optimizationProblem;
    private String sqlText;
    private String sqlFingerprint;
    private String executionPlanJson;
    private String slowSqlMetricsJson;
    private String optimizationContent;
    private BigDecimal confidence;
    private String processStatus;
    private String executedSql;
    private Long executedBy;
    private LocalDateTime executedAt;
    private String executeResult;
    private String beforeMetricsJson;
    private String afterMetricsJson;
    private Long handledBy;
    private LocalDateTime handledAt;
    @TableField(exist = false)
    private String creatorName;

    public String getReportCode() { return reportCode; }
    public void setReportCode(String reportCode) { this.reportCode = reportCode; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public Long getExecutionId() { return executionId; }
    public void setExecutionId(Long executionId) { this.executionId = executionId; }
    public String getTaskNameSnapshot() { return taskNameSnapshot; }
    public void setTaskNameSnapshot(String taskNameSnapshot) { this.taskNameSnapshot = taskNameSnapshot; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public String getTargetNameSnapshot() { return targetNameSnapshot; }
    public void setTargetNameSnapshot(String targetNameSnapshot) { this.targetNameSnapshot = targetNameSnapshot; }
    public String getOptimizationType() { return optimizationType; }
    public void setOptimizationType(String optimizationType) { this.optimizationType = optimizationType; }
    public String getOptimizationProblem() { return optimizationProblem; }
    public void setOptimizationProblem(String optimizationProblem) { this.optimizationProblem = optimizationProblem; }
    public String getSqlText() { return sqlText; }
    public void setSqlText(String sqlText) { this.sqlText = sqlText; }
    public String getSqlFingerprint() { return sqlFingerprint; }
    public void setSqlFingerprint(String sqlFingerprint) { this.sqlFingerprint = sqlFingerprint; }
    public String getExecutionPlanJson() { return executionPlanJson; }
    public void setExecutionPlanJson(String executionPlanJson) { this.executionPlanJson = executionPlanJson; }
    public String getSlowSqlMetricsJson() { return slowSqlMetricsJson; }
    public void setSlowSqlMetricsJson(String slowSqlMetricsJson) { this.slowSqlMetricsJson = slowSqlMetricsJson; }
    public String getOptimizationContent() { return optimizationContent; }
    public void setOptimizationContent(String optimizationContent) { this.optimizationContent = optimizationContent; }
    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }
    public String getProcessStatus() { return processStatus; }
    public void setProcessStatus(String processStatus) { this.processStatus = processStatus; }
    public String getExecutedSql() { return executedSql; }
    public void setExecutedSql(String executedSql) { this.executedSql = executedSql; }
    public Long getExecutedBy() { return executedBy; }
    public void setExecutedBy(Long executedBy) { this.executedBy = executedBy; }
    public LocalDateTime getExecutedAt() { return executedAt; }
    public void setExecutedAt(LocalDateTime executedAt) { this.executedAt = executedAt; }
    public String getExecuteResult() { return executeResult; }
    public void setExecuteResult(String executeResult) { this.executeResult = executeResult; }
    public String getBeforeMetricsJson() { return beforeMetricsJson; }
    public void setBeforeMetricsJson(String beforeMetricsJson) { this.beforeMetricsJson = beforeMetricsJson; }
    public String getAfterMetricsJson() { return afterMetricsJson; }
    public void setAfterMetricsJson(String afterMetricsJson) { this.afterMetricsJson = afterMetricsJson; }
    public Long getHandledBy() { return handledBy; }
    public void setHandledBy(Long handledBy) { this.handledBy = handledBy; }
    public LocalDateTime getHandledAt() { return handledAt; }
    public void setHandledAt(LocalDateTime handledAt) { this.handledAt = handledAt; }
    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
}
