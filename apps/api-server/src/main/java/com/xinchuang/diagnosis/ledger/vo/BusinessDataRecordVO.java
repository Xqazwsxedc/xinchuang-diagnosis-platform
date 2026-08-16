package com.xinchuang.diagnosis.ledger.vo;

import java.time.LocalDateTime;

public class BusinessDataRecordVO {
    private Long id;
    private LocalDateTime collectedAt;
    private Long totalSizeBytes;
    private String collectStatus;
    private String fileName;
    private String recordSummary;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getCollectedAt() { return collectedAt; }
    public void setCollectedAt(LocalDateTime collectedAt) { this.collectedAt = collectedAt; }
    public Long getTotalSizeBytes() { return totalSizeBytes; }
    public void setTotalSizeBytes(Long totalSizeBytes) { this.totalSizeBytes = totalSizeBytes; }
    public String getCollectStatus() { return collectStatus; }
    public void setCollectStatus(String collectStatus) { this.collectStatus = collectStatus; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getRecordSummary() { return recordSummary; }
    public void setRecordSummary(String recordSummary) { this.recordSummary = recordSummary; }
}
