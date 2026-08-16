package com.xinchuang.diagnosis.ledger.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("report_export_log")
public class ReportExportLog {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String reportType;
    private Long reportId;
    private String fileName;
    private String fileUrl;
    private String exportStatus;
    private String failureReason;
    private Long exportedBy;
    private LocalDateTime exportedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public String getExportStatus() { return exportStatus; }
    public void setExportStatus(String exportStatus) { this.exportStatus = exportStatus; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public Long getExportedBy() { return exportedBy; }
    public void setExportedBy(Long exportedBy) { this.exportedBy = exportedBy; }
    public LocalDateTime getExportedAt() { return exportedAt; }
    public void setExportedAt(LocalDateTime exportedAt) { this.exportedAt = exportedAt; }
}
