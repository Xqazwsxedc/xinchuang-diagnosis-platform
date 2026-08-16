package com.xinchuang.diagnosis.dashboard.vo;

public class TrendPointVO {
    private String time;
    private long businessSystemCount;
    private long databaseCount;
    private long abnormalCount;

    public TrendPointVO() {
    }

    public TrendPointVO(String time, long businessSystemCount, long databaseCount, long abnormalCount) {
        this.time = time;
        this.businessSystemCount = businessSystemCount;
        this.databaseCount = databaseCount;
        this.abnormalCount = abnormalCount;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

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

    public long getAbnormalCount() {
        return abnormalCount;
    }

    public void setAbnormalCount(long abnormalCount) {
        this.abnormalCount = abnormalCount;
    }
}
