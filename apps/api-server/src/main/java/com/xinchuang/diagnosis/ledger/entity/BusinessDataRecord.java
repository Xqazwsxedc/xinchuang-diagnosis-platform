package com.xinchuang.diagnosis.ledger.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xinchuang.diagnosis.common.entity.BaseEntity;
import java.time.LocalDateTime;

@TableName("bus_system_data_record")
public class BusinessDataRecord extends BaseEntity {
    private Long systemId;
    private LocalDateTime collectedAt;
    private Long totalSizeBytes;
    private String collectStatus;
    private String fileName;
    private String recordSummary;

    public Long getSystemId() { return systemId; }
    public void setSystemId(Long systemId) { this.systemId = systemId; }
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
