package com.xinchuang.diagnosis.ai.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xinchuang.diagnosis.common.entity.BaseEntity;
import java.time.LocalDateTime;

@TableName("ai_task")
public class AiTask extends BaseEntity {
    private String taskCode;
    private String taskName;
    private String taskType;
    private Long modelId;
    private String modelNameSnapshot;
    private String targetType;
    private Long targetId;
    private String targetNameSnapshot;
    private String metricCodes;
    private String paramJson;
    private String scheduleRule;
    private String status;
    private LocalDateTime lastExecTime;
    private LocalDateTime nextExecTime;
    private Integer executionCount;
    private Integer resultCount;
    @TableField(exist = false)
    private String creatorName;

    public String getTaskCode() {
        return taskCode;
    }

    public void setTaskCode(String taskCode) {
        this.taskCode = taskCode;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public String getModelNameSnapshot() {
        return modelNameSnapshot;
    }

    public void setModelNameSnapshot(String modelNameSnapshot) {
        this.modelNameSnapshot = modelNameSnapshot;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getTargetNameSnapshot() {
        return targetNameSnapshot;
    }

    public void setTargetNameSnapshot(String targetNameSnapshot) {
        this.targetNameSnapshot = targetNameSnapshot;
    }

    public String getMetricCodes() {
        return metricCodes;
    }

    public void setMetricCodes(String metricCodes) {
        this.metricCodes = metricCodes;
    }

    public String getParamJson() {
        return paramJson;
    }

    public void setParamJson(String paramJson) {
        this.paramJson = paramJson;
    }

    public String getScheduleRule() {
        return scheduleRule;
    }

    public void setScheduleRule(String scheduleRule) {
        this.scheduleRule = scheduleRule;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getLastExecTime() {
        return lastExecTime;
    }

    public void setLastExecTime(LocalDateTime lastExecTime) {
        this.lastExecTime = lastExecTime;
    }

    public LocalDateTime getNextExecTime() {
        return nextExecTime;
    }

    public void setNextExecTime(LocalDateTime nextExecTime) {
        this.nextExecTime = nextExecTime;
    }

    public Integer getExecutionCount() {
        return executionCount;
    }

    public void setExecutionCount(Integer executionCount) {
        this.executionCount = executionCount;
    }

    public Integer getResultCount() {
        return resultCount;
    }

    public void setResultCount(Integer resultCount) {
        this.resultCount = resultCount;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }
}
