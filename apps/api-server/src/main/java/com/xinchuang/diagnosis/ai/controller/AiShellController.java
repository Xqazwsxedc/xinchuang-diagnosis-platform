package com.xinchuang.diagnosis.ai.controller;

import com.xinchuang.diagnosis.common.api.ApiResponse;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
public class AiShellController {

    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> overview() {
        return ApiResponse.ok(Map.of(
            "faultPrediction", "UI_ONLY",
            "defectDetection", "UI_ONLY",
            "performanceOptimization", "UI_ONLY"
        ));
    }
}

