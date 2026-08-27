package com.xinchuang.diagnosis.ai.engine;

public interface FaultPredictionEngine {
    TimeSeriesInferenceResponse predict(TimeSeriesInferenceRequest request);
}
