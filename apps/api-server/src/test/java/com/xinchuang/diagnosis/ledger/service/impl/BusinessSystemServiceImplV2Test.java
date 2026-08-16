package com.xinchuang.diagnosis.ledger.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xinchuang.diagnosis.common.exception.BusinessException;
import com.xinchuang.diagnosis.ledger.dto.BusinessSystemSaveRequest;
import com.xinchuang.diagnosis.ledger.entity.BusinessSystem;
import com.xinchuang.diagnosis.ledger.entity.BusinessSystemDb;
import com.xinchuang.diagnosis.ledger.entity.DbResource;
import com.xinchuang.diagnosis.ledger.mapper.BusinessSystemDbMapper;
import com.xinchuang.diagnosis.ledger.mapper.BusinessSystemMapper;
import com.xinchuang.diagnosis.ledger.mapper.DbResourceMapper;
import com.xinchuang.diagnosis.system.service.AuditLogService;
import com.xinchuang.diagnosis.system.service.NotificationService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BusinessSystemServiceImplV2Test {
    @Mock
    private BusinessSystemMapper businessSystemMapper;
    @Mock
    private BusinessSystemDbMapper businessSystemDbMapper;
    @Mock
    private DbResourceMapper dbResourceMapper;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private NotificationService notificationService;

    private BusinessSystemServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new BusinessSystemServiceImpl(businessSystemMapper, businessSystemDbMapper, dbResourceMapper,
            auditLogService, notificationService);
    }

    @Test
    void createRejectsRequestWithoutDatabaseRelationBeforeWritingData() {
        BusinessException exception = assertThrows(BusinessException.class,
            () -> service.create(request(null, List.of())));

        assertEquals("请选择至少一个关联分布式数据库", exception.getMessage());
        verify(businessSystemMapper, never()).insert(any(BusinessSystem.class));
        verify(businessSystemDbMapper, never()).insert(any(BusinessSystemDb.class));
    }

    @Test
    void createRejectsMissingRelatedDatabaseBeforeWritingData() {
        when(dbResourceMapper.selectBatchIds(List.of(7L, 8L))).thenReturn(List.of(resource(7L)));

        BusinessException exception = assertThrows(BusinessException.class,
            () -> service.create(request(null, List.of(7L, 8L))));

        assertEquals("关联的分布式数据库不存在", exception.getMessage());
        verify(businessSystemMapper, never()).insert(any(BusinessSystem.class));
        verify(businessSystemDbMapper, never()).insert(any(BusinessSystemDb.class));
    }

    @Test
    void detailRejectsMissingBusinessSystem() {
        when(businessSystemMapper.selectById(99L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.detail(99L));

        assertEquals("业务系统不存在", exception.getMessage());
    }

    @Test
    void deleteRemovesRelationsThenBusinessSystemAndWritesAuditLog() {
        BusinessSystem entity = new BusinessSystem();
        entity.setId(1L);
        entity.setName("测试业务系统");
        when(businessSystemMapper.selectById(1L)).thenReturn(entity);

        service.delete(1L);

        verify(businessSystemDbMapper).deleteByBusinessSystemIdPhysically(1L);
        verify(businessSystemMapper).deleteById(1L);
        verify(auditLogService).recordSuccess("数据台账", "DELETE", "BUSINESS_SYSTEM", 1L, "删除业务系统：测试业务系统");
    }

    private BusinessSystemSaveRequest request(Long databaseId, List<Long> databaseIds) {
        BusinessSystemSaveRequest request = new BusinessSystemSaveRequest();
        request.setName("测试业务系统");
        request.setOwnerName("测试负责人");
        request.setStatus("NORMAL");
        request.setDatabaseId(databaseId);
        request.setDatabaseIds(databaseIds);
        return request;
    }

    private DbResource resource(Long id) {
        DbResource resource = new DbResource();
        resource.setId(id);
        resource.setName("测试数据库");
        return resource;
    }
}
