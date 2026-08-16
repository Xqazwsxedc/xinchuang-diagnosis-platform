package com.xinchuang.diagnosis.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xinchuang.diagnosis.common.entity.BaseEntity;

@TableName("kb_instance")
public class KnowledgeInstance extends BaseEntity {
    private String knowledgeCode;
    private String knowledgeName;
    private String knowledgeType;
    private String knowledgeContent;
    private String attachmentNames;
    private String sourceType;
    private String sourceResultType;
    private Long sourceResultId;
    private String status;
    private Integer version;

    public String getKnowledgeCode() {
        return knowledgeCode;
    }

    public void setKnowledgeCode(String knowledgeCode) {
        this.knowledgeCode = knowledgeCode;
    }

    public String getKnowledgeName() {
        return knowledgeName;
    }

    public void setKnowledgeName(String knowledgeName) {
        this.knowledgeName = knowledgeName;
    }

    public String getKnowledgeType() {
        return knowledgeType;
    }

    public void setKnowledgeType(String knowledgeType) {
        this.knowledgeType = knowledgeType;
    }

    public String getKnowledgeContent() {
        return knowledgeContent;
    }

    public void setKnowledgeContent(String knowledgeContent) {
        this.knowledgeContent = knowledgeContent;
    }

    public String getAttachmentNames() {
        return attachmentNames;
    }

    public void setAttachmentNames(String attachmentNames) {
        this.attachmentNames = attachmentNames;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceResultType() {
        return sourceResultType;
    }

    public void setSourceResultType(String sourceResultType) {
        this.sourceResultType = sourceResultType;
    }

    public Long getSourceResultId() {
        return sourceResultId;
    }

    public void setSourceResultId(Long sourceResultId) {
        this.sourceResultId = sourceResultId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
