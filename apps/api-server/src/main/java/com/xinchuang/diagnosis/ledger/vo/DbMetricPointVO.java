package com.xinchuang.diagnosis.ledger.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DbMetricPointVO {
    private LocalDateTime sampledAt;
    private BigDecimal cpuUsage;
    private BigDecimal memoryUsage;
    private Integer diskIops;
    private Integer currentConnections;
    private BigDecimal cacheHitRate;

    public LocalDateTime getSampledAt() { return sampledAt; }
    public void setSampledAt(LocalDateTime sampledAt) { this.sampledAt = sampledAt; }
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
}
