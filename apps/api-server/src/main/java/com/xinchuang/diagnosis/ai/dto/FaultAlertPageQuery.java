package com.xinchuang.diagnosis.ai.dto;

import com.xinchuang.diagnosis.common.query.PageQuery;
import jakarta.validation.constraints.Pattern;

public class FaultAlertPageQuery extends PageQuery {
    private String keyword;
    @Pattern(regexp = "^(|PENDING|CONFIRMED|FALSE_POSITIVE|CLOSED)$", message = "处理状态不合法")
    private String processStatus;
    @Pattern(regexp = "^(|LOW|MEDIUM|HIGH|CRITICAL)$", message = "风险等级不合法")
    private String riskLevel;
    @Pattern(regexp = "^(|BUSINESS_SYSTEM|DB_RESOURCE)$", message = "预测对象类型不合法")
    private String targetType;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public String getProcessStatus() { return processStatus; }
    public void setProcessStatus(String processStatus) { this.processStatus = processStatus; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
}
