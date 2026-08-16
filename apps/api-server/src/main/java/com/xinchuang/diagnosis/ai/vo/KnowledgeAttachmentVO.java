package com.xinchuang.diagnosis.ai.vo;

public class KnowledgeAttachmentVO {
    private Long id;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private boolean previewable;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public boolean isPreviewable() { return previewable; }
    public void setPreviewable(boolean previewable) { this.previewable = previewable; }
}
