package com.xinchuang.diagnosis.ledger.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xinchuang.diagnosis.common.entity.BaseEntity;
import java.time.LocalDateTime;

@TableName("db_instance_log")
public class DbInstanceLog extends BaseEntity {
    private Long dbInstanceId;
    private LocalDateTime loggedAt;
    private String logLevel;
    private String logContent;

    public Long getDbInstanceId() { return dbInstanceId; }
    public void setDbInstanceId(Long dbInstanceId) { this.dbInstanceId = dbInstanceId; }
    public LocalDateTime getLoggedAt() { return loggedAt; }
    public void setLoggedAt(LocalDateTime loggedAt) { this.loggedAt = loggedAt; }
    public String getLogLevel() { return logLevel; }
    public void setLogLevel(String logLevel) { this.logLevel = logLevel; }
    public String getLogContent() { return logContent; }
    public void setLogContent(String logContent) { this.logContent = logContent; }
}
