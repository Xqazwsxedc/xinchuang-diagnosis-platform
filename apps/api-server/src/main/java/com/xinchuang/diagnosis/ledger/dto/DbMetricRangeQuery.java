package com.xinchuang.diagnosis.ledger.dto;

import jakarta.validation.constraints.Pattern;

public class DbMetricRangeQuery {
    @Pattern(regexp = "1H|3H|6H|12H|1D|3D|7D|14D", message = "时间范围不合法")
    private String range = "1D";

    public String getRange() { return range; }
    public void setRange(String range) { this.range = range; }
}
