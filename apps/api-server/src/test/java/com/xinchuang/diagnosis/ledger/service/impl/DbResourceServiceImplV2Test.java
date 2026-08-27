package com.xinchuang.diagnosis.ledger.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xinchuang.diagnosis.common.exception.BusinessException;
import com.xinchuang.diagnosis.ledger.entity.DbResource;
import com.xinchuang.diagnosis.ledger.mapper.BusinessSystemDbMapper;
import com.xinchuang.diagnosis.ledger.mapper.BusinessSystemMapper;
import com.xinchuang.diagnosis.ledger.mapper.DbMonitoringConfigMapper;
import com.xinchuang.diagnosis.ledger.mapper.DbResourceMapper;
import com.xinchuang.diagnosis.system.service.AuditLogService;
import com.xinchuang.diagnosis.system.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DbResourceServiceImplV2Test {
    @Mock
    private DbResourceMapper dbResourceMapper;
    @Mock
    private DbMonitoringConfigMapper dbMonitoringConfigMapper;
    @Mock
    private BusinessSystemMapper businessSystemMapper;
    @Mock
    private BusinessSystemDbMapper businessSystemDbMapper;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private NotificationService notificationService;

    private DbResourceServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DbResourceServiceImpl(dbResourceMapper, dbMonitoringConfigMapper, businessSystemMapper,
            businessSystemDbMapper, auditLogService, notificationService);
    }

    @Test
    void detailRejectsMissingDatabaseResource() {
        when(dbResourceMapper.selectById(99L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.detail(99L));

        assertEquals("分布式数据库不存在", exception.getMessage());
    }

    @Test
    void deleteRejectsDatabaseUsedAsBusinessSystemPrimaryDatabase() {
        when(dbResourceMapper.selectById(1L)).thenReturn(resource());
        when(businessSystemMapper.selectCount(any())).thenReturn(1L);
        when(businessSystemDbMapper.selectCount(any())).thenReturn(0L);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.delete(1L));

        assertEquals("该分布式数据库已被业务系统关联，不能删除", exception.getMessage());
        verify(dbResourceMapper, never()).deleteById(1L);
        verify(auditLogService, never()).recordSuccess(any(), any(), any(), any(), any());
    }

    @Test
    void deleteRejectsDatabaseUsedByRelationTable() {
        when(dbResourceMapper.selectById(1L)).thenReturn(resource());
        when(businessSystemMapper.selectCount(any())).thenReturn(0L);
        when(businessSystemDbMapper.selectCount(any())).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.delete(1L));

        assertEquals("该分布式数据库已被业务系统关联，不能删除", exception.getMessage());
        verify(dbResourceMapper, never()).deleteById(1L);
    }

    @Test
    void deleteRemovesUnrelatedDatabaseAndWritesAuditLog() {
        when(dbResourceMapper.selectById(1L)).thenReturn(resource());
        when(businessSystemMapper.selectCount(any())).thenReturn(0L);
        when(businessSystemDbMapper.selectCount(any())).thenReturn(0L);

        service.delete(1L);

        verify(dbResourceMapper).deleteById(1L);
        verify(auditLogService).recordSuccess("数据台账", "DELETE", "DB_RESOURCE", 1L, "删除分布式数据库：测试数据库");
    }

    private DbResource resource() {
        DbResource resource = new DbResource();
        resource.setId(1L);
        resource.setName("测试数据库");
        return resource;
    }
}
