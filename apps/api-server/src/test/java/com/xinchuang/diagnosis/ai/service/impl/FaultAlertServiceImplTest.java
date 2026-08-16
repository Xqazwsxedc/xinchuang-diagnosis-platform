package com.xinchuang.diagnosis.ai.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.xinchuang.diagnosis.ai.dto.ResultHandleRequest;
import com.xinchuang.diagnosis.ai.mapper.ResultStatusLogMapper;
import com.xinchuang.diagnosis.ai.service.AiManagementService;
import com.xinchuang.diagnosis.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
/** 验证故障预警专用服务保留必需的处理安全边界。 */
class FaultAlertServiceImplTest {
    @Mock private AiManagementService aiManagementService;
    @Mock private ResultStatusLogMapper resultStatusLogMapper;
    @InjectMocks private FaultAlertServiceImpl service;

    @Test
    /** 空白处理备注必须在当前服务拒绝，不能调用下游状态更新。 */
    void handleRequiresProcessingRemark() {
        ResultHandleRequest request = new ResultHandleRequest();
        request.setProcessStatus("CONFIRMED");
        request.setRemark("  ");

        BusinessException exception = assertThrows(BusinessException.class, () -> service.handle(1L, request));

        assertEquals("处理备注不能为空", exception.getMessage());
        verify(aiManagementService, never()).handleFaultReport(1L, request);
    }
}
