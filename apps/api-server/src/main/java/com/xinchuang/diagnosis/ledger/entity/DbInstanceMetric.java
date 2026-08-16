package com.xinchuang.diagnosis.ledger.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xinchuang.diagnosis.common.entity.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("db_instance_metric")
public class DbInstanceMetric extends BaseEntity {
    private Long dbInstanceId;
    private LocalDateTime sampledAt;
    private BigDecimal cpuUsage;
    private BigDecimal memoryUsage;
    private Integer diskIops;
    private BigDecimal cacheHitRate;

    public Long getDbInstanceId() { return dbInstanceId; }
    public void setDbInstanceId(Long dbInstanceId) { this.dbInstanceId = dbInstanceId; }
    public LocalDateTime getSampledAt() { return sampledAt; }
    public void setSampledAt(LocalDateTime sampledAt) { this.sampledAt = sampledAt; }
    public BigDecimal getCpuUsage() { return cpuUsage; }
    public void setCpuUsage(BigDecimal cpuUsage) { this.cpuUsage = cpuUsage; }
    public BigDecimal getMemoryUsage() { return memoryUsage; }
    public void setMemoryUsage(BigDecimal memoryUsage) { this.memoryUsage = memoryUsage; }
    public Integer getDiskIops() { return diskIops; }
    public void setDiskIops(Integer diskIops) { this.diskIops = diskIops; }
    public BigDecimal getCacheHitRate() { return cacheHitRate; }
    public void setCacheHitRate(BigDecimal cacheHitRate) { this.cacheHitRate = cacheHitRate; }
}
