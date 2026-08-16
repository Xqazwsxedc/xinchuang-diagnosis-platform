package com.xinchuang.diagnosis.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xinchuang.diagnosis.common.entity.BaseEntity;

@TableName("kb_attachment")
public class KnowledgeAttachment extends BaseEntity {
    private Long knowledgeId;
    private String originalFilename;
    private String storedFilename;
    private String storagePath;
    private String contentType;
    private String fileExtension;
    private Long fileSize;

    public Long getKnowledgeId() { return knowledgeId; }
    public void setKnowledgeId(Long knowledgeId) { this.knowledgeId = knowledgeId; }
    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
    public String getStoredFilename() { return storedFilename; }
    public void setStoredFilename(String storedFilename) { this.storedFilename = storedFilename; }
    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public String getFileExtension() { return fileExtension; }
    public void setFileExtension(String fileExtension) { this.fileExtension = fileExtension; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
}
