package com.xinchuang.diagnosis.ledger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public class BusinessSystemSaveRequest {
    @NotBlank
    @Size(max = 100)
    private String name;

    @Positive(message = "关联分布式数据库 ID 必须大于 0")
    private Long databaseId;

    @Size(max = 100, message = "最多关联 100 个分布式数据库")
    private List<@Positive(message = "关联分布式数据库 ID 必须大于 0") Long> databaseIds;

    @NotBlank
    @Size(max = 64)
    private String ownerName;

    @NotBlank
    @Pattern(regexp = "NORMAL|WARNING|DANGER|OFFLINE", message = "状态只能是 NORMAL/WARNING/DANGER/OFFLINE")
    private String status;

    @Size(max = 255)
    private String accessUrl;

    @Size(max = 500)
    private String remark;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(Long databaseId) {
        this.databaseId = databaseId;
    }

    public List<Long> getDatabaseIds() {
        return databaseIds;
    }

    public void setDatabaseIds(List<Long> databaseIds) {
        this.databaseIds = databaseIds;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAccessUrl() {
        return accessUrl;
    }

    public void setAccessUrl(String accessUrl) {
        this.accessUrl = accessUrl;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
