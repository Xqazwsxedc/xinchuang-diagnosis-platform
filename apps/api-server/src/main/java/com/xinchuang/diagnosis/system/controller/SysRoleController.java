package com.xinchuang.diagnosis.system.controller;

import com.xinchuang.diagnosis.common.api.ApiResponse;
import com.xinchuang.diagnosis.common.api.PageResponse;
import com.xinchuang.diagnosis.system.dto.RolePageQuery;
import com.xinchuang.diagnosis.system.dto.RoleMenuAssignRequest;
import com.xinchuang.diagnosis.system.dto.RoleSaveRequest;
import com.xinchuang.diagnosis.system.dto.RoleStatusRequest;
import com.xinchuang.diagnosis.system.service.SysMenuService;
import com.xinchuang.diagnosis.system.service.SysRoleService;
import com.xinchuang.diagnosis.system.vo.RoleVO;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/system/roles")
public class SysRoleController {
    private final SysRoleService sysRoleService;
    private final SysMenuService sysMenuService;

    public SysRoleController(SysRoleService sysRoleService, SysMenuService sysMenuService) {
        this.sysRoleService = sysRoleService;
        this.sysMenuService = sysMenuService;
    }

    @GetMapping
    public ApiResponse<PageResponse<RoleVO>> page(@Valid @ModelAttribute RolePageQuery query) {
        return ApiResponse.ok(sysRoleService.page(query));
    }

    @GetMapping("/{id}")
    public ApiResponse<RoleVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(sysRoleService.detail(id));
    }

    @PostMapping
    public ApiResponse<RoleVO> create(@Valid @RequestBody RoleSaveRequest request) {
        return ApiResponse.ok(sysRoleService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<RoleVO> update(@PathVariable Long id, @Valid @RequestBody RoleSaveRequest request) {
        return ApiResponse.ok(sysRoleService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> status(@PathVariable Long id, @Valid @RequestBody RoleStatusRequest request) {
        sysRoleService.updateStatus(id, request);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        sysRoleService.delete(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}/menus")
    public ApiResponse<List<Long>> roleMenus(@PathVariable Long id) {
        return ApiResponse.ok(sysMenuService.getRoleMenuIds(id));
    }

    @PutMapping("/{id}/menus")
    public ApiResponse<Void> assignMenus(@PathVariable Long id, @Valid @RequestBody RoleMenuAssignRequest request) {
        sysMenuService.assignRoleMenus(id, request.getMenuIds());
        return ApiResponse.ok(null);
    }
}
