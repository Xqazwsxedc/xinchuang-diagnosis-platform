package com.xinchuang.diagnosis.ai.vo;

import java.time.LocalDateTime;
import java.util.List;

public class KnowledgeVO {
    private Long id;
    private String knowledgeCode;
    private String knowledgeName;
    private String knowledgeType;
    private String knowledgeContent;
    private String creatorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<KnowledgeAttachmentVO> attachments;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getKnowledgeCode() { return knowledgeCode; }
    public void setKnowledgeCode(String knowledgeCode) { this.knowledgeCode = knowledgeCode; }
    public String getKnowledgeName() { return knowledgeName; }
    public void setKnowledgeName(String knowledgeName) { this.knowledgeName = knowledgeName; }
    public String getKnowledgeType() { return knowledgeType; }
    public void setKnowledgeType(String knowledgeType) { this.knowledgeType = knowledgeType; }
    public String getKnowledgeContent() { return knowledgeContent; }
    public void setKnowledgeContent(String knowledgeContent) { this.knowledgeContent = knowledgeContent; }
    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<KnowledgeAttachmentVO> getAttachments() { return attachments; }
    public void setAttachments(List<KnowledgeAttachmentVO> attachments) { this.attachments = attachments; }
}
