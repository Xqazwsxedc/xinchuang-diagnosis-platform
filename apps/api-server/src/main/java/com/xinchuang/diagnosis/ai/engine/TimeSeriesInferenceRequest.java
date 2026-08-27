package com.xinchuang.diagnosis.ai.engine;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TimeSeriesInferenceRequest {
    @NotBlank(message = "契约版本不能为空")
    private String contractVersion = "1.0";
    @NotBlank(message = "请求ID不能为空")
    private String requestId;
    @NotBlank(message = "模型编码不能为空")
    private String modelCode = "zscore-communication-test";
    @NotEmpty(message = "时间戳不能为空")
    @Size(min = 3, max = 20000, message = "时间点数量必须在3到20000之间")
    private List<@NotNull(message = "时间戳不能为空") Long> timestamps;
    @NotEmpty(message = "时序特征不能为空")
    @Size(max = 22, message = "时序特征不能超过22项")
    private Map<@NotBlank(message = "指标编码不能为空") String,
        List<@NotNull(message = "指标值不能为空") Double>> features = new LinkedHashMap<>();
    @NotNull(message = "异常阈值不能为空")
    @DecimalMin(value = "0.0", inclusive = false, message = "异常阈值必须大于0")
    private Double threshold = 1.5;

    public String getContractVersion() { return contractVersion; }
    public void setContractVersion(String contractVersion) { this.contractVersion = contractVersion; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getModelCode() { return modelCode; }
    public void setModelCode(String modelCode) { this.modelCode = modelCode; }
    public List<Long> getTimestamps() { return timestamps; }
    public void setTimestamps(List<Long> timestamps) { this.timestamps = timestamps; }
    public Map<String, List<Double>> getFeatures() { return features; }
    public void setFeatures(Map<String, List<Double>> features) { this.features = features; }
    public Double getThreshold() { return threshold; }
    public void setThreshold(Double threshold) { this.threshold = threshold; }
}
