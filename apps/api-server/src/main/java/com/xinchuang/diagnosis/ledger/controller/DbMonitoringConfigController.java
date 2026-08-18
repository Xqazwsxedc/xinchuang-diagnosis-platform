package com.xinchuang.diagnosis.ledger.controller;

import com.xinchuang.diagnosis.common.api.ApiResponse;
import com.xinchuang.diagnosis.ledger.dto.DbMonitoringConfigRequest;
import com.xinchuang.diagnosis.ledger.service.DbMonitoringConfigService;
import com.xinchuang.diagnosis.ledger.vo.DbMonitoringConfigVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ledger/db-resources/{id}/monitoring-config")
public class DbMonitoringConfigController {
    private final DbMonitoringConfigService service;
    public DbMonitoringConfigController(DbMonitoringConfigService service) { this.service = service; }
    @GetMapping
    public ApiResponse<DbMonitoringConfigVO> get(@PathVariable Long id) { return ApiResponse.ok(service.get(id)); }
    @PutMapping
    public ApiResponse<DbMonitoringConfigVO> save(@PathVariable Long id, @Valid @RequestBody DbMonitoringConfigRequest request) {
        return ApiResponse.ok(service.save(id, request));
    }
    @PostMapping("/test")
    public ApiResponse<DbMonitoringConfigVO> test(@PathVariable Long id) { return ApiResponse.ok(service.test(id)); }
}
