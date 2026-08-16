package com.xinchuang.diagnosis.ai.dto;

import jakarta.validation.constraints.Pattern;

public class TaskExecuteRequest {
    @Pattern(regexp = "MANUAL|SCHEDULED", message = "触发方式只能是 MANUAL/SCHEDULED")
    private String triggerType = "MANUAL";

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }
}
