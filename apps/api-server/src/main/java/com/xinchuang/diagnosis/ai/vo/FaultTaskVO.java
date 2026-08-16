package com.xinchuang.diagnosis.ai.vo;

import com.xinchuang.diagnosis.ai.entity.AiTask;
import java.time.LocalDateTime;

public class FaultTaskVO {
    private Long id;
    private String taskCode;
    private String taskName;
    private Long modelId;
    private String modelNameSnapshot;
    private String targetType;
    private Long targetId;
    private String targetNameSnapshot;
    private String metricCodes;
    private String scheduleRule;
    private String status;
    private String creatorName;
    private LocalDateTime createdAt;
    private LocalDateTime lastExecTime;
    private Integer executionCount;
    private Integer resultCount;

    public static FaultTaskVO from(AiTask task) {
        FaultTaskVO vo = new FaultTaskVO();
        vo.populate(task);
        return vo;
    }

    protected void populate(AiTask task) {
        id = task.getId(); taskCode = task.getTaskCode(); taskName = task.getTaskName();
        modelId = task.getModelId(); modelNameSnapshot = task.getModelNameSnapshot();
        targetType = task.getTargetType(); targetId = task.getTargetId(); targetNameSnapshot = task.getTargetNameSnapshot();
        metricCodes = task.getMetricCodes(); scheduleRule = task.getScheduleRule(); status = task.getStatus();
        creatorName = task.getCreatorName(); createdAt = task.getCreatedAt(); lastExecTime = task.getLastExecTime();
        executionCount = task.getExecutionCount(); resultCount = task.getResultCount();
    }
    public Long getId() { return id; } public String getTaskCode() { return taskCode; } public String getTaskName() { return taskName; }
    public Long getModelId() { return modelId; } public String getModelNameSnapshot() { return modelNameSnapshot; } public String getTargetType() { return targetType; }
    public Long getTargetId() { return targetId; } public String getTargetNameSnapshot() { return targetNameSnapshot; } public String getMetricCodes() { return metricCodes; }
    public String getScheduleRule() { return scheduleRule; } public String getStatus() { return status; } public String getCreatorName() { return creatorName; }
    public LocalDateTime getCreatedAt() { return createdAt; } public LocalDateTime getLastExecTime() { return lastExecTime; }
    public Integer getExecutionCount() { return executionCount; } public Integer getResultCount() { return resultCount; }
}
