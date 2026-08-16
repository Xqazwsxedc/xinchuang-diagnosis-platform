package com.xinchuang.diagnosis.dashboard.vo;

import java.time.LocalDateTime;

public class AlertItemVO {
    private String sourceType;
    private Long sourceId;
    private String sourceName;
    private String level;
    private String message;
    private LocalDateTime updatedAt;

    public AlertItemVO() {
    }

    public AlertItemVO(String sourceType, Long sourceId, String sourceName, String level, String message, LocalDateTime updatedAt) {
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.sourceName = sourceName;
        this.level = level;
        this.message = message;
        this.updatedAt = updatedAt;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
