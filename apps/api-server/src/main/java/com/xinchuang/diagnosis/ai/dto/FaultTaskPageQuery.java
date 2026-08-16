package com.xinchuang.diagnosis.ai.dto;

import com.xinchuang.diagnosis.common.query.PageQuery;
import jakarta.validation.constraints.Pattern;

public class FaultTaskPageQuery extends PageQuery {
    private String keyword;
    private Long modelId;
    private String creatorName;
    @Pattern(regexp = "^$|\\d{4}-\\d{2}-\\d{2}$", message = "创建日期格式应为 yyyy-MM-dd")
    private String createdDate;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public Long getModelId() { return modelId; }
    public void setModelId(Long modelId) { this.modelId = modelId; }
    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }
}
