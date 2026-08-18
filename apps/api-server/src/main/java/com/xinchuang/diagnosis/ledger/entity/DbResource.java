package com.xinchuang.diagnosis.ledger.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xinchuang.diagnosis.common.entity.BaseEntity;

@TableName("ledger_db_resource")
public class DbResource extends BaseEntity {
    private String name;
    private String publicIp;
    private String privateIp;
    private String ownerName;
    private String status;
    private String monitorStatus;
    private String monitorMessage;
    private java.time.LocalDateTime monitorLastCheckedAt;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPublicIp() {
        return publicIp;
    }

    public void setPublicIp(String publicIp) {
        this.publicIp = publicIp;
    }

    public String getPrivateIp() {
        return privateIp;
    }

    public void setPrivateIp(String privateIp) {
        this.privateIp = privateIp;
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

    public String getMonitorStatus() { return monitorStatus; }
    public void setMonitorStatus(String monitorStatus) { this.monitorStatus = monitorStatus; }
    public String getMonitorMessage() { return monitorMessage; }
    public void setMonitorMessage(String monitorMessage) { this.monitorMessage = monitorMessage; }
    public java.time.LocalDateTime getMonitorLastCheckedAt() { return monitorLastCheckedAt; }
    public void setMonitorLastCheckedAt(java.time.LocalDateTime monitorLastCheckedAt) { this.monitorLastCheckedAt = monitorLastCheckedAt; }
}
