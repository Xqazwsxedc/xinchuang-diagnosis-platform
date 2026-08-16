package com.xinchuang.diagnosis.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ResultHandleRequest {
    @NotBlank
    @Pattern(regexp = "PENDING|CONFIRMED|FALSE_POSITIVE|CLOSED", message = "处理状态只能是 PENDING/CONFIRMED/FALSE_POSITIVE/CLOSED")
    private String processStatus;

    @Size(max = 500)
    private String remark;

    public String getProcessStatus() {
        return processStatus;
    }

    public void setProcessStatus(String processStatus) {
        this.processStatus = processStatus;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
