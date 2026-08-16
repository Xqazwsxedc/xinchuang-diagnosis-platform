package com.xinchuang.diagnosis.ledger.vo;

import java.math.BigDecimal;
import java.util.List;

public class DbMetricsOverviewVO {
    private BigDecimal cpuUsage;
    private BigDecimal memoryUsage;
    private Integer diskIops;
    private Integer currentConnections;
    private BigDecimal cacheHitRate;
    private List<DbMetricPointVO> points;

    public BigDecimal getCpuUsage() { return cpuUsage; }
    public void setCpuUsage(BigDecimal cpuUsage) { this.cpuUsage = cpuUsage; }
    public BigDecimal getMemoryUsage() { return memoryUsage; }
    public void setMemoryUsage(BigDecimal memoryUsage) { this.memoryUsage = memoryUsage; }
    public Integer getDiskIops() { return diskIops; }
    public void setDiskIops(Integer diskIops) { this.diskIops = diskIops; }
    public Integer getCurrentConnections() { return currentConnections; }
    public void setCurrentConnections(Integer currentConnections) { this.currentConnections = currentConnections; }
    public BigDecimal getCacheHitRate() { return cacheHitRate; }
    public void setCacheHitRate(BigDecimal cacheHitRate) { this.cacheHitRate = cacheHitRate; }
    public List<DbMetricPointVO> getPoints() { return points; }
    public void setPoints(List<DbMetricPointVO> points) { this.points = points; }
}
