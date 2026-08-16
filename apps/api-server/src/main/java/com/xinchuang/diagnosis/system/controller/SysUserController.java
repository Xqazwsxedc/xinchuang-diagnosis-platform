package com.xinchuang.diagnosis.system.controller;

import com.xinchuang.diagnosis.common.api.ApiResponse;
import com.xinchuang.diagnosis.common.api.PageResponse;
import com.xinchuang.diagnosis.system.dto.ResetPasswordRequest;
import com.xinchuang.diagnosis.system.dto.UserCreateRequest;
import com.xinchuang.diagnosis.system.dto.UserPageQuery;
import com.xinchuang.diagnosis.system.dto.UserStatusRequest;
import com.xinchuang.diagnosis.system.dto.UserUpdateRequest;
import com.xinchuang.diagnosis.system.service.SysUserService;
import com.xinchuang.diagnosis.system.vo.RoleVO;
import com.xinchuang.diagnosis.system.vo.UserVO;
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
@RequestMapping("/system/users")
public class SysUserController {
    private final SysUserService sysUserService;

    public SysUserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @GetMapping
    public ApiResponse<PageResponse<UserVO>> page(@Valid @ModelAttribute UserPageQuery query) {
        return ApiResponse.ok(sysUserService.page(query));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(sysUserService.detail(id));
    }

    @PostMapping
    public ApiResponse<UserVO> create(@Valid @RequestBody UserCreateRequest request) {
        return ApiResponse.ok(sysUserService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserVO> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return ApiResponse.ok(sysUserService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> status(@PathVariable Long id, @Valid @RequestBody UserStatusRequest request) {
        sysUserService.updateStatus(id, request);
        return ApiResponse.ok(null);
    }

    @PatchMapping("/{id}/password")
    public ApiResponse<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest request) {
        sysUserService.resetPassword(id, request);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        sysUserService.delete(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/roles")
    public ApiResponse<List<RoleVO>> roles() {
        return ApiResponse.ok(sysUserService.roles());
    }
}
