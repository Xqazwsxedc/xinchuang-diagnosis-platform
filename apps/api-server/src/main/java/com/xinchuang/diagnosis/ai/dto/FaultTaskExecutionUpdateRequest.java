package com.xinchuang.diagnosis.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;

/** Internal contract for a future authenticated AI engine adapter; it is not exposed as a public HTTP endpoint yet. */
public class FaultTaskExecutionUpdateRequest {
    @NotBlank(message = "执行状态不能为空")
    @Pattern(regexp = "RUNNING|SUCCESS|FAILED", message = "执行状态只能是 RUNNING/SUCCESS/FAILED")
    private String executionStatus;
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer resultCount;
    private String errorMessage;

    public String getExecutionStatus() { return executionStatus; }
    public void setExecutionStatus(String executionStatus) { this.executionStatus = executionStatus; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public Integer getResultCount() { return resultCount; }
    public void setResultCount(Integer resultCount) { this.resultCount = resultCount; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
