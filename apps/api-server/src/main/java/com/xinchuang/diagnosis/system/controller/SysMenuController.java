package com.xinchuang.diagnosis.system.controller;

import com.xinchuang.diagnosis.common.api.ApiResponse;
import com.xinchuang.diagnosis.system.dto.MenuSaveRequest;
import com.xinchuang.diagnosis.system.dto.MenuStatusRequest;
import com.xinchuang.diagnosis.system.service.SysMenuService;
import com.xinchuang.diagnosis.system.vo.MenuVO;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/menus")
public class SysMenuController {
    private final SysMenuService sysMenuService;

    public SysMenuController(SysMenuService sysMenuService) {
        this.sysMenuService = sysMenuService;
    }

    @GetMapping("/tree")
    public ApiResponse<List<MenuVO>> tree() {
        return ApiResponse.ok(sysMenuService.tree());
    }

    @GetMapping("/{id}")
    public ApiResponse<MenuVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(sysMenuService.detail(id));
    }

    @PostMapping
    public ApiResponse<MenuVO> create(@Valid @RequestBody MenuSaveRequest request) {
        return ApiResponse.ok(sysMenuService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<MenuVO> update(@PathVariable Long id, @Valid @RequestBody MenuSaveRequest request) {
        return ApiResponse.ok(sysMenuService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> status(@PathVariable Long id, @Valid @RequestBody MenuStatusRequest request) {
        sysMenuService.updateStatus(id, request);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        sysMenuService.delete(id);
        return ApiResponse.ok(null);
    }
}
