package com.xinchuang.diagnosis.system.controller;

import com.xinchuang.diagnosis.common.api.ApiResponse;
import com.xinchuang.diagnosis.common.api.PageResponse;
import com.xinchuang.diagnosis.system.dto.ConfigPageQuery;
import com.xinchuang.diagnosis.system.dto.ConfigSaveRequest;
import com.xinchuang.diagnosis.system.dto.SimpleStatusRequest;
import com.xinchuang.diagnosis.system.service.SysConfigService;
import com.xinchuang.diagnosis.system.vo.ConfigVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/configs")
public class SysConfigController {
    private final SysConfigService sysConfigService;

    public SysConfigController(SysConfigService sysConfigService) {
        this.sysConfigService = sysConfigService;
    }

    @GetMapping
    public ApiResponse<PageResponse<ConfigVO>> page(@Valid @ModelAttribute ConfigPageQuery query) {
        return ApiResponse.ok(sysConfigService.page(query));
    }

    @GetMapping("/{id}")
    public ApiResponse<ConfigVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(sysConfigService.detail(id));
    }

    @GetMapping("/key/{configKey}")
    public ApiResponse<ConfigVO> getByKey(@PathVariable String configKey) {
        return ApiResponse.ok(sysConfigService.getByKey(configKey));
    }

    @PostMapping
    public ApiResponse<ConfigVO> create(@Valid @RequestBody ConfigSaveRequest request) {
        return ApiResponse.ok(sysConfigService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ConfigVO> update(@PathVariable Long id, @Valid @RequestBody ConfigSaveRequest request) {
        return ApiResponse.ok(sysConfigService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody SimpleStatusRequest request) {
        sysConfigService.updateStatus(id, request);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        sysConfigService.delete(id);
        return ApiResponse.ok(null);
    }
}
