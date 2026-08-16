package com.xinchuang.diagnosis.ai.dto;

import java.util.ArrayList;
import java.util.List;

public class KnowledgeSaveRequest {
    private String knowledgeCode;
    private String knowledgeName;
    private String knowledgeType;
    private String knowledgeContent;
    private List<Long> retainedAttachmentIds = new ArrayList<>();

    public String getKnowledgeCode() { return knowledgeCode; }
    public void setKnowledgeCode(String knowledgeCode) { this.knowledgeCode = knowledgeCode; }
    public String getKnowledgeName() { return knowledgeName; }
    public void setKnowledgeName(String knowledgeName) { this.knowledgeName = knowledgeName; }
    public String getKnowledgeType() { return knowledgeType; }
    public void setKnowledgeType(String knowledgeType) { this.knowledgeType = knowledgeType; }
    public String getKnowledgeContent() { return knowledgeContent; }
    public void setKnowledgeContent(String knowledgeContent) { this.knowledgeContent = knowledgeContent; }
    public List<Long> getRetainedAttachmentIds() { return retainedAttachmentIds; }
    public void setRetainedAttachmentIds(List<Long> retainedAttachmentIds) {
        this.retainedAttachmentIds = retainedAttachmentIds == null ? new ArrayList<>() : retainedAttachmentIds;
    }
}
