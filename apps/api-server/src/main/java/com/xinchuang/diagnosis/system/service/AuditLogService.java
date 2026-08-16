package com.xinchuang.diagnosis.system.service;

import com.xinchuang.diagnosis.common.api.PageResponse;
import com.xinchuang.diagnosis.system.dto.LoginLogPageQuery;
import com.xinchuang.diagnosis.system.dto.OperationLogPageQuery;
import com.xinchuang.diagnosis.system.vo.LoginLogVO;
import com.xinchuang.diagnosis.system.vo.OperationLogVO;

public interface AuditLogService {
    void recordLogin(Long userId, String username, String loginStatus, String failureReason);

    void recordSuccess(String moduleName, String operationType, String targetType, Long targetId, String operationContent);

    PageResponse<LoginLogVO> pageLoginLogs(LoginLogPageQuery query);

    PageResponse<OperationLogVO> pageOperationLogs(OperationLogPageQuery query);
}
