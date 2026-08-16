package com.xinchuang.diagnosis.ledger.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xinchuang.diagnosis.common.entity.BaseEntity;

@TableName("ledger_business_system")
public class BusinessSystem extends BaseEntity {
    private String name;
    private Long databaseId;
    private String ownerName;
    private String status;
    private String accessUrl;
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

