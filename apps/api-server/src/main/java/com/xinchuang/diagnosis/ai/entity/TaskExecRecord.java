package com.xinchuang.diagnosis.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xinchuang.diagnosis.common.entity.BaseEntity;
import java.time.LocalDateTime;

@TableName("task_exec_record")
public class TaskExecRecord extends BaseEntity {
    private Long taskId;
    private String taskType;
    private String taskNameSnapshot;
    private String executionStatus;
    private String triggerType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime inputStartTime;
    private LocalDateTime inputEndTime;
    private Integer resultCount;
    private String errorMessage;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getTaskNameSnapshot() {
        return taskNameSnapshot;
    }

    public void setTaskNameSnapshot(String taskNameSnapshot) {
        this.taskNameSnapshot = taskNameSnapshot;
    }

    public String getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(String executionStatus) {
        this.executionStatus = executionStatus;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getInputStartTime() {
        return inputStartTime;
    }

    public void setInputStartTime(LocalDateTime inputStartTime) {
        this.inputStartTime = inputStartTime;
    }

    public LocalDateTime getInputEndTime() {
        return inputEndTime;
    }

    public void setInputEndTime(LocalDateTime inputEndTime) {
        this.inputEndTime = inputEndTime;
    }

    public Integer getResultCount() {
        return resultCount;
    }

    public void setResultCount(Integer resultCount) {
        this.resultCount = resultCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
