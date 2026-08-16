package com.xinchuang.diagnosis.ledger.vo;

import java.time.LocalDateTime;

public class DbInstanceLogVO {
    private Long id;
    private LocalDateTime loggedAt;
    private String logLevel;
    private String logContent;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getLoggedAt() { return loggedAt; }
    public void setLoggedAt(LocalDateTime loggedAt) { this.loggedAt = loggedAt; }
    public String getLogLevel() { return logLevel; }
    public void setLogLevel(String logLevel) { this.logLevel = logLevel; }
    public String getLogContent() { return logContent; }
    public void setLogContent(String logContent) { this.logContent = logContent; }
}
