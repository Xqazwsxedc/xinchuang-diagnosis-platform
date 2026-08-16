package com.xinchuang.diagnosis.ai.controller;

import com.xinchuang.diagnosis.ai.dto.FaultAlertPageQuery;
import com.xinchuang.diagnosis.ai.dto.ResultHandleRequest;
import com.xinchuang.diagnosis.ai.service.FaultAlertService;
import com.xinchuang.diagnosis.ai.vo.FaultAlertDetailVO;
import com.xinchuang.diagnosis.ai.vo.FaultAlertVO;
import com.xinchuang.diagnosis.common.api.ApiResponse;
import com.xinchuang.diagnosis.common.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai/fault-prediction/alerts")
public class FaultAlertController {
    private final FaultAlertService faultAlertService;

    public FaultAlertController(FaultAlertService faultAlertService) { this.faultAlertService = faultAlertService; }

    @GetMapping
    public ApiResponse<PageResponse<FaultAlertVO>> page(@Valid @ModelAttribute FaultAlertPageQuery query) {
        return ApiResponse.ok(faultAlertService.page(query));
    }

    @GetMapping("/{id}")
    public ApiResponse<FaultAlertDetailVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(faultAlertService.detail(id));
    }

    @PatchMapping("/{id}/handle")
    public ApiResponse<FaultAlertDetailVO> handle(@PathVariable Long id, @Valid @RequestBody ResultHandleRequest request) {
        return ApiResponse.ok(faultAlertService.handle(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        faultAlertService.delete(id);
        return ApiResponse.ok(null);
    }
}
