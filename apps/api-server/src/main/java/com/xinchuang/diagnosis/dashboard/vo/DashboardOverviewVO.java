package com.xinchuang.diagnosis.dashboard.vo;

import java.util.Collections;
import java.util.List;

public class DashboardOverviewVO {
    private long businessSystemCount;
    private long databaseCount;
    private long normalCount;
    private long abnormalCount;
    private int healthScore;
    private List<StatusDistributionVO> statusDistribution = Collections.emptyList();
    private List<TrendPointVO> trend = Collections.emptyList();
    private List<AlertItemVO> alerts = Collections.emptyList();

    public long getBusinessSystemCount() {
        return businessSystemCount;
    }

    public void setBusinessSystemCount(long businessSystemCount) {
        this.businessSystemCount = businessSystemCount;
    }

    public long getDatabaseCount() {
        return databaseCount;
    }

    public void setDatabaseCount(long databaseCount) {
        this.databaseCount = databaseCount;
    }

    public long getNormalCount() {
        return normalCount;
    }

    public void setNormalCount(long normalCount) {
        this.normalCount = normalCount;
    }

    public long getAbnormalCount() {
        return abnormalCount;
    }

    public void setAbnormalCount(long abnormalCount) {
        this.abnormalCount = abnormalCount;
    }

    public int getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(int healthScore) {
        this.healthScore = healthScore;
    }

    public List<StatusDistributionVO> getStatusDistribution() {
        return statusDistribution;
    }

    public void setStatusDistribution(List<StatusDistributionVO> statusDistribution) {
        this.statusDistribution = statusDistribution;
    }

    public List<TrendPointVO> getTrend() {
        return trend;
    }

    public void setTrend(List<TrendPointVO> trend) {
        this.trend = trend;
    }

    public List<AlertItemVO> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<AlertItemVO> alerts) {
        this.alerts = alerts;
    }
}
