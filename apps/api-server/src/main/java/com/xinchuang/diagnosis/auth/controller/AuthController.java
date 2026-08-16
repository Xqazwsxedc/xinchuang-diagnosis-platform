package com.xinchuang.diagnosis.auth.controller;

import com.xinchuang.diagnosis.auth.dto.LoginRequest;
import com.xinchuang.diagnosis.auth.service.AuthService;
import com.xinchuang.diagnosis.auth.vo.LoginResponseVO;
import com.xinchuang.diagnosis.auth.vo.LoginUserVO;
import com.xinchuang.diagnosis.common.api.ApiResponse;
import com.xinchuang.diagnosis.system.dto.ChangePasswordRequest;
import com.xinchuang.diagnosis.system.dto.ProfileUpdateRequest;
import com.xinchuang.diagnosis.system.service.SysUserService;
import com.xinchuang.diagnosis.system.vo.UserVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final SysUserService sysUserService;

    public AuthController(AuthService authService, SysUserService sysUserService) {
        this.authService = authService;
        this.sysUserService = sysUserService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponseVO> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.ok(null);
    }

    @GetMapping("/me")
    public ApiResponse<LoginUserVO> me() {
        return ApiResponse.ok(authService.currentUser());
    }

    @GetMapping("/profile")
    public ApiResponse<UserVO> profile() {
        return ApiResponse.ok(sysUserService.currentProfile());
    }

    @PutMapping("/profile")
    public ApiResponse<UserVO> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        return ApiResponse.ok(sysUserService.updateCurrentProfile(request));
    }

    @PatchMapping("/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        sysUserService.changeCurrentPassword(request);
        return ApiResponse.ok(null);
    }
}
