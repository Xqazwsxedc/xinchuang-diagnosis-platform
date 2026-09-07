package com.xinchuang.diagnosis.ai.engine;

import java.time.LocalDateTime;

public record FaultPredictionInput(TimeSeriesInferenceRequest request,
                                   LocalDateTime inputStartTime,
                                   LocalDateTime inputEndTime) { }
