package com.xinchuang.diagnosis.system.controller;

import com.xinchuang.diagnosis.common.api.ApiResponse;
import com.xinchuang.diagnosis.common.api.PageResponse;
import com.xinchuang.diagnosis.system.dto.LoginLogPageQuery;
import com.xinchuang.diagnosis.system.service.AuditLogService;
import com.xinchuang.diagnosis.system.vo.LoginLogVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/security")
public class SecurityLoginController {
    private final AuditLogService auditLogService;

    public SecurityLoginController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/login-logs")
    public ApiResponse<PageResponse<LoginLogVO>> loginLogs(@Valid @ModelAttribute LoginLogPageQuery query) {
        return ApiResponse.ok(auditLogService.pageLoginLogs(query));
    }
}
