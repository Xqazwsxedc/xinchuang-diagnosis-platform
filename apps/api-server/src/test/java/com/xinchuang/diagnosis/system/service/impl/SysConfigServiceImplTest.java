package com.xinchuang.diagnosis.system.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xinchuang.diagnosis.common.exception.BusinessException;
import com.xinchuang.diagnosis.system.dto.ConfigSaveRequest;
import com.xinchuang.diagnosis.system.entity.SysConfig;
import com.xinchuang.diagnosis.system.mapper.SysConfigMapper;
import com.xinchuang.diagnosis.system.service.AuditLogService;
import com.xinchuang.diagnosis.system.vo.ConfigVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SysConfigServiceImplTest {
    @Mock
    private SysConfigMapper sysConfigMapper;
    @Mock
    private AuditLogService auditLogService;

    private SysConfigServiceImpl configService;

    @BeforeEach
    void setUp() {
        configService = new SysConfigServiceImpl(sysConfigMapper, auditLogService);
    }

    @Test
    void createRejectsDuplicateConfigKeyWithoutWritingData() {
        SysConfig existing = new SysConfig();
        existing.setId(1L);
        existing.setConfigKey("SYSTEM_NAME");
        when(sysConfigMapper.selectOne(any())).thenReturn(existing);

        ConfigSaveRequest request = request("SYSTEM_NAME");
        BusinessException exception = assertThrows(BusinessException.class, () -> configService.create(request));

        assertEquals("配置键已存在", exception.getMessage());
        verify(sysConfigMapper, never()).insert(any(SysConfig.class));
        verify(auditLogService, never()).recordSuccess(any(), any(), any(), any(), any());
    }

    @Test
    void createPersistsConfigAndRecordsAuditLog() {
        SysConfig[] saved = new SysConfig[1];
        when(sysConfigMapper.selectOne(any())).thenReturn(null);
        doAnswer(invocation -> {
            saved[0] = invocation.getArgument(0);
            saved[0].setId(99L);
            return 1;
        }).when(sysConfigMapper).insert(any(SysConfig.class));
        when(sysConfigMapper.selectById(99L)).thenAnswer(invocation -> saved[0]);

        ConfigVO result = configService.create(request("SYSTEM_NAME"));

        assertEquals(99L, result.getId());
        assertEquals("SYSTEM_NAME", result.getConfigKey());
        assertEquals("TEXT", result.getConfigType());
        verify(auditLogService).recordSuccess("系统配置", "CREATE", "SYS_CONFIG", 99L, "新增系统配置：SYSTEM_NAME");
    }

    @Test
    void updateRejectsMissingConfig() {
        when(sysConfigMapper.selectById(99L)).thenReturn(null);

        ConfigSaveRequest request = request("SYSTEM_NAME");
        BusinessException exception = assertThrows(BusinessException.class, () -> configService.update(99L, request));

        assertEquals("系统配置不存在", exception.getMessage());
        verify(sysConfigMapper, never()).updateById(any(SysConfig.class));
    }

    private ConfigSaveRequest request(String key) {
        ConfigSaveRequest request = new ConfigSaveRequest();
        request.setConfigKey(key);
        request.setConfigName("系统名称");
        request.setConfigValue("信创数据库智能故障诊断及修复平台");
        request.setConfigType("TEXT");
        request.setStatus("ENABLED");
        return request;
    }
}
