package com.xinchuang.diagnosis.system.controller;

import com.xinchuang.diagnosis.common.api.ApiResponse;
import com.xinchuang.diagnosis.common.api.PageResponse;
import com.xinchuang.diagnosis.system.dto.LoginLogPageQuery;
import com.xinchuang.diagnosis.system.dto.OperationLogPageQuery;
import com.xinchuang.diagnosis.system.service.AuditLogService;
import com.xinchuang.diagnosis.system.vo.LoginLogVO;
import com.xinchuang.diagnosis.system.vo.OperationLogVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/logs")
public class AuditLogController {
    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/login")
    public ApiResponse<PageResponse<LoginLogVO>> loginLogs(@Valid @ModelAttribute LoginLogPageQuery query) {
        return ApiResponse.ok(auditLogService.pageLoginLogs(query));
    }

    @GetMapping("/operation")
    public ApiResponse<PageResponse<OperationLogVO>> operationLogs(@Valid @ModelAttribute OperationLogPageQuery query) {
        return ApiResponse.ok(auditLogService.pageOperationLogs(query));
    }
}
