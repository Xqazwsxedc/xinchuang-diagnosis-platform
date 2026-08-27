package com.xinchuang.diagnosis.ai.controller;

import com.xinchuang.diagnosis.ai.engine.FaultPredictionEngine;
import com.xinchuang.diagnosis.ai.engine.TimeSeriesInferenceRequest;
import com.xinchuang.diagnosis.ai.engine.TimeSeriesInferenceResponse;
import com.xinchuang.diagnosis.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai/fault-prediction/engine")
public class FaultPredictionEngineController {
    private final FaultPredictionEngine engine;

    public FaultPredictionEngineController(FaultPredictionEngine engine) {
        this.engine = engine;
    }

    @PostMapping("/verify")
    public ApiResponse<TimeSeriesInferenceResponse> verify(
            @Valid @RequestBody TimeSeriesInferenceRequest request) {
        return ApiResponse.ok(engine.predict(request));
    }
}
