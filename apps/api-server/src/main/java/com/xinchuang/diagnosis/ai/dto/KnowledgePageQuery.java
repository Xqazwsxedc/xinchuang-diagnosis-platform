package com.xinchuang.diagnosis.ai.dto;

import com.xinchuang.diagnosis.common.query.PageQuery;
import java.time.LocalDate;

public class KnowledgePageQuery extends PageQuery {
    private String knowledgeId;
    private String knowledgeName;
    private String creatorName;
    private LocalDate createdDate;
    private String knowledgeType;

    public String getKnowledgeId() { return knowledgeId; }
    public void setKnowledgeId(String knowledgeId) { this.knowledgeId = knowledgeId; }
    public String getKnowledgeName() { return knowledgeName; }
    public void setKnowledgeName(String knowledgeName) { this.knowledgeName = knowledgeName; }
    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }
    public String getKnowledgeType() { return knowledgeType; }
    public void setKnowledgeType(String knowledgeType) { this.knowledgeType = knowledgeType; }
}
