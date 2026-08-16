package com.xinchuang.diagnosis.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinchuang.diagnosis.common.api.PageResponse;
import com.xinchuang.diagnosis.security.LoginUser;
import com.xinchuang.diagnosis.security.UserContext;
import com.xinchuang.diagnosis.system.dto.LoginLogPageQuery;
import com.xinchuang.diagnosis.system.dto.OperationLogPageQuery;
import com.xinchuang.diagnosis.system.entity.LoginLog;
import com.xinchuang.diagnosis.system.entity.OperationLog;
import com.xinchuang.diagnosis.system.mapper.LoginLogMapper;
import com.xinchuang.diagnosis.system.mapper.OperationLogMapper;
import com.xinchuang.diagnosis.system.service.AuditLogService;
import com.xinchuang.diagnosis.system.vo.LoginLogVO;
import com.xinchuang.diagnosis.system.vo.OperationLogVO;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class AuditLogServiceImpl implements AuditLogService {
    private final LoginLogMapper loginLogMapper;
    private final OperationLogMapper operationLogMapper;

    public AuditLogServiceImpl(LoginLogMapper loginLogMapper, OperationLogMapper operationLogMapper) {
        this.loginLogMapper = loginLogMapper;
        this.operationLogMapper = operationLogMapper;
    }

    @Override
    public void recordLogin(Long userId, String username, String loginStatus, String failureReason) {
        LoginLog log = new LoginLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setLoginTime(LocalDateTime.now());
        log.setLoginIp(resolveClientIp());
        log.setDeviceInfo(resolveUserAgent());
        log.setLoginStatus(loginStatus);
        log.setFailureReason(failureReason);
        loginLogMapper.insert(log);
    }

    @Override
    public void recordSuccess(String moduleName, String operationType, String targetType, Long targetId, String operationContent) {
        LoginUser loginUser = UserContext.get();
        OperationLog log = new OperationLog();
        if (loginUser != null) {
            log.setOperatorId(loginUser.getUserId());
            log.setOperatorNameSnapshot(loginUser.getRealName());
        }
        log.setModuleName(moduleName);
        log.setOperationType(operationType);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setOperationContent(operationContent);
        log.setOperationResult("SUCCESS");
        log.setRequestIp(resolveClientIp());
        log.setUserAgent(resolveUserAgent());
        operationLogMapper.insert(log);
    }

    @Override
    public PageResponse<LoginLogVO> pageLoginLogs(LoginLogPageQuery query) {
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(LoginLog::getUsername, query.getKeyword());
        }
        if (StringUtils.hasText(query.getUsername())) {
            wrapper.like(LoginLog::getUsername, query.getUsername());
        }
        if (StringUtils.hasText(query.getLoginStatus())) {
            wrapper.eq(LoginLog::getLoginStatus, query.getLoginStatus());
        }
        if (query.getStartTime() != null) {
            wrapper.ge(LoginLog::getLoginTime, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(LoginLog::getLoginTime, query.getEndTime());
        }
        wrapper.orderByDesc(LoginLog::getLoginTime);
        Page<LoginLog> page = loginLogMapper.selectPage(new Page<>(query.getPageNo(), query.getPageSize()), wrapper);
        List<LoginLogVO> records = page.getRecords().stream().map(this::toLoginVO).toList();
        return PageResponse.of(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public PageResponse<OperationLogVO> pageOperationLogs(OperationLogPageQuery query) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(OperationLog::getOperatorNameSnapshot, query.getKeyword())
                .or()
                .like(OperationLog::getOperationContent, query.getKeyword())
                .or()
                .like(OperationLog::getModuleName, query.getKeyword()));
        }
        if (StringUtils.hasText(query.getOperatorName())) {
            wrapper.like(OperationLog::getOperatorNameSnapshot, query.getOperatorName());
        }
        if (StringUtils.hasText(query.getModuleName())) {
            wrapper.eq(OperationLog::getModuleName, query.getModuleName());
        }
        if (StringUtils.hasText(query.getOperationType())) {
            wrapper.eq(OperationLog::getOperationType, query.getOperationType());
        }
        if (StringUtils.hasText(query.getOperationResult())) {
            wrapper.eq(OperationLog::getOperationResult, query.getOperationResult());
        }
        if (query.getStartTime() != null) {
            wrapper.ge(OperationLog::getCreatedAt, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(OperationLog::getCreatedAt, query.getEndTime());
        }
        wrapper.orderByDesc(OperationLog::getCreatedAt);
        Page<OperationLog> page = operationLogMapper.selectPage(new Page<>(query.getPageNo(), query.getPageSize()), wrapper);
        List<OperationLogVO> records = page.getRecords().stream().map(this::toOperationVO).toList();
        return PageResponse.of(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    private LoginLogVO toLoginVO(LoginLog log) {
        LoginLogVO vo = new LoginLogVO();
        vo.setId(log.getId());
        vo.setUserId(log.getUserId());
        vo.setUsername(log.getUsername());
        vo.setLoginTime(log.getLoginTime());
        vo.setLoginIp(log.getLoginIp());
        vo.setDeviceInfo(log.getDeviceInfo());
        vo.setLoginStatus(log.getLoginStatus());
        vo.setFailureReason(log.getFailureReason());
        return vo;
    }

    private OperationLogVO toOperationVO(OperationLog log) {
        OperationLogVO vo = new OperationLogVO();
        vo.setId(log.getId());
        vo.setOperatorId(log.getOperatorId());
        vo.setOperatorNameSnapshot(log.getOperatorNameSnapshot());
        vo.setModuleName(log.getModuleName());
        vo.setOperationType(log.getOperationType());
        vo.setTargetType(log.getTargetType());
        vo.setTargetId(log.getTargetId());
        vo.setOperationContent(log.getOperationContent());
        vo.setOperationResult(log.getOperationResult());
        vo.setErrorMessage(log.getErrorMessage());
        vo.setRequestIp(log.getRequestIp());
        vo.setUserAgent(log.getUserAgent());
        vo.setCreatedAt(log.getCreatedAt());
        return vo;
    }

    private String resolveClientIp() {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return null;
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp;
        }
        return request.getRemoteAddr();
    }

    private String resolveUserAgent() {
        HttpServletRequest request = currentRequest();
        return request == null ? null : request.getHeader("User-Agent");
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }
}
