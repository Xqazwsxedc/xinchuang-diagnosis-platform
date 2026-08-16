package com.xinchuang.diagnosis.ledger.dto;

import com.xinchuang.diagnosis.common.query.PageQuery;
import jakarta.validation.constraints.Pattern;

public class DbInstanceLogPageQuery extends PageQuery {
    @Pattern(regexp = "1H|3H|6H|12H|1D|3D|7D|14D", message = "时间范围不合法")
    private String range = "1D";

    @Pattern(regexp = "INFO|WARN|ERROR", message = "日志级别不合法")
    private String logLevel;

    public String getRange() { return range; }
    public void setRange(String range) { this.range = range; }
    public String getLogLevel() { return logLevel; }
    public void setLogLevel(String logLevel) { this.logLevel = logLevel; }
}
