package com.xinchuang.diagnosis.ai.engine;

import java.util.ArrayList;
import java.util.List;

public class TimeSeriesInferenceResponse {
    private String contractVersion;
    private String requestId;
    private String engine;
    private String modelCode;
    private String modelVersion;
    private Integer pointCount;
    private List<AnomalyPoint> anomalies = new ArrayList<>();

    public String getContractVersion() { return contractVersion; }
    public void setContractVersion(String contractVersion) { this.contractVersion = contractVersion; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getEngine() { return engine; }
    public void setEngine(String engine) { this.engine = engine; }
    public String getModelCode() { return modelCode; }
    public void setModelCode(String modelCode) { this.modelCode = modelCode; }
    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
    public Integer getPointCount() { return pointCount; }
    public void setPointCount(Integer pointCount) { this.pointCount = pointCount; }
    public List<AnomalyPoint> getAnomalies() { return anomalies; }
    public void setAnomalies(List<AnomalyPoint> anomalies) { this.anomalies = anomalies; }

    public static class AnomalyPoint {
        private String metricCode;
        private Integer index;
        private Long timestamp;
        private Double value;
        private Double score;

        public String getMetricCode() { return metricCode; }
        public void setMetricCode(String metricCode) { this.metricCode = metricCode; }
        public Integer getIndex() { return index; }
        public void setIndex(Integer index) { this.index = index; }
        public Long getTimestamp() { return timestamp; }
        public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
        public Double getValue() { return value; }
        public void setValue(Double value) { this.value = value; }
        public Double getScore() { return score; }
        public void setScore(Double score) { this.score = score; }
    }
}
