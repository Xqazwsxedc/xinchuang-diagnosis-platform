package com.xinchuang.diagnosis.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.xinchuang.diagnosis.common.entity.BaseEntity;
import java.time.LocalDateTime;

@TableName("defect_report")
public class DefectReport extends BaseEntity {
    private String defectCode;
    private Long taskId;
    private Long executionId;
    private String taskNameSnapshot;
    private String targetType;
    private Long targetId;
    private String targetNameSnapshot;
    private String defectType;
    private String severityLevel;
    private String defectProblem;
    private String defectData;
    private String defectDescription;
    private String repairSuggestion;
    private String processStatus;
    private Long handledBy;
    private LocalDateTime handledAt;
    @TableField(exist = false)
    private String creatorName;

    public String getDefectCode() { return defectCode; }
    public void setDefectCode(String defectCode) { this.defectCode = defectCode; }
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
    public String getDefectType() { return defectType; }
    public void setDefectType(String defectType) { this.defectType = defectType; }
    public String getSeverityLevel() { return severityLevel; }
    public void setSeverityLevel(String severityLevel) { this.severityLevel = severityLevel; }
    public String getDefectProblem() { return defectProblem; }
    public void setDefectProblem(String defectProblem) { this.defectProblem = defectProblem; }
    public String getDefectData() { return defectData; }
    public void setDefectData(String defectData) { this.defectData = defectData; }
    public String getDefectDescription() { return defectDescription; }
    public void setDefectDescription(String defectDescription) { this.defectDescription = defectDescription; }
    public String getRepairSuggestion() { return repairSuggestion; }
    public void setRepairSuggestion(String repairSuggestion) { this.repairSuggestion = repairSuggestion; }
    public String getProcessStatus() { return processStatus; }
    public void setProcessStatus(String processStatus) { this.processStatus = processStatus; }
    public Long getHandledBy() { return handledBy; }
    public void setHandledBy(Long handledBy) { this.handledBy = handledBy; }
    public LocalDateTime getHandledAt() { return handledAt; }
    public void setHandledAt(LocalDateTime handledAt) { this.handledAt = handledAt; }
    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
}
