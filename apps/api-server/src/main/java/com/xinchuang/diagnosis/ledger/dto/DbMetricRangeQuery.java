package com.xinchuang.diagnosis.ledger.dto;

import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

public class DbMetricRangeQuery {
    @Pattern(regexp = "1H|3H|6H|12H|1D|3D|7D|14D", message = "时间范围不合法")
    private String range = "1D";
    private LocalDate startDate;
    private LocalDate endDate;
    private String metrics;

    public String getRange() { return range; }
    public void setRange(String range) { this.range = range; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getMetrics() { return metrics; }
    public void setMetrics(String metrics) { this.metrics = metrics; }
}
