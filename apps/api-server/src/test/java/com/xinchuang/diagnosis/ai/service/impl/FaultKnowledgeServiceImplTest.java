package com.xinchuang.diagnosis.ai.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xinchuang.diagnosis.ai.dto.KnowledgeSaveRequest;
import com.xinchuang.diagnosis.ai.dto.KnowledgePageQuery;
import com.xinchuang.diagnosis.ai.entity.KnowledgeInstance;
import com.xinchuang.diagnosis.ai.mapper.KnowledgeAttachmentMapper;
import com.xinchuang.diagnosis.ai.mapper.KnowledgeInstanceMapper;
import com.xinchuang.diagnosis.ai.service.KnowledgeFileStorageService;
import com.xinchuang.diagnosis.common.exception.BusinessException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinchuang.diagnosis.common.api.PageResponse;
import com.xinchuang.diagnosis.ai.vo.KnowledgeVO;
import com.xinchuang.diagnosis.system.mapper.SysUserMapper;
import com.xinchuang.diagnosis.system.service.AuditLogService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
/**
 * 在不写入文件系统的前提下，验证知识库参数校验和历史数据兼容能力。
 * 为支持安全升级，历史记录创建人为空的情况必须被允许。
 */
class FaultKnowledgeServiceImplTest {
    @Mock private KnowledgeInstanceMapper knowledgeMapper;
    @Mock private KnowledgeAttachmentMapper attachmentMapper;
    @Mock private KnowledgeFileStorageService fileStorageService;
    @Mock private SysUserMapper userMapper;
    @Mock private AuditLogService auditLogService;
    private FaultKnowledgeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new FaultKnowledgeServiceImpl(knowledgeMapper, attachmentMapper, fileStorageService, userMapper, auditLogService);
    }

    @Test
    /** 知识名称必填校验必须先于知识记录插入执行。 */
    void createRejectsBlankKnowledgeNameBeforeWritingData() {
        KnowledgeSaveRequest request = validRequest();
        request.setKnowledgeName(" ");

        BusinessException exception = assertThrows(BusinessException.class, () -> service.create(request, List.of(), true));

        assertEquals("知识名称不能为空", exception.getMessage());
        verify(knowledgeMapper, never()).insert(any(KnowledgeInstance.class));
    }

    @Test
    /** 新知识至少保留或上传一个附件，否则不能保存。 */
    void createRejectsMissingRequiredAttachmentBeforeWritingData() {
        BusinessException exception = assertThrows(BusinessException.class, () -> service.create(validRequest(), List.of(), true));

        assertEquals("请至少上传一个附件", exception.getMessage());
        verify(knowledgeMapper, never()).insert(any(KnowledgeInstance.class));
    }

    @Test
    /** 详情不存在时应返回业务异常，不能暴露空指针错误。 */
    void detailRejectsMissingKnowledge() {
        when(knowledgeMapper.selectById(99L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.detail(99L));

        assertEquals("知识不存在或已删除", exception.getMessage());
    }

    @Test
    /** 审计字段补齐前创建的历史记录可能没有创建人，但仍必须正常列表展示。 */
    void pageSupportsLegacyKnowledgeWithoutCreator() {
        KnowledgeInstance legacyKnowledge = legacyKnowledge();
        Page<KnowledgeInstance> page = new Page<>(1, 10);
        page.setRecords(List.of(legacyKnowledge));
        page.setTotal(1);
        when(knowledgeMapper.selectPage(any(Page.class), any())).thenReturn(page);

        PageResponse<KnowledgeVO> response = assertDoesNotThrow(() -> service.page(new KnowledgePageQuery()));

        assertEquals(1, response.getRecords().size());
        assertNull(response.getRecords().get(0).getCreatorName());
        verify(userMapper, never()).selectBatchIds(any());
    }

    @Test
    /** 历史空创建人兼容规则同样适用于直接查询详情。 */
    void detailSupportsLegacyKnowledgeWithoutCreator() {
        KnowledgeInstance legacyKnowledge = legacyKnowledge();
        when(knowledgeMapper.selectById(1L)).thenReturn(legacyKnowledge);
        when(attachmentMapper.selectList(any())).thenReturn(List.of());

        KnowledgeVO response = assertDoesNotThrow(() -> service.detail(1L));

        assertNull(response.getCreatorName());
        verify(userMapper, never()).selectBatchIds(any());
    }

    private KnowledgeInstance legacyKnowledge() {
        KnowledgeInstance knowledge = new KnowledgeInstance();
        knowledge.setId(1L);
        knowledge.setKnowledgeCode("FAULT-KB-LEGACY");
        knowledge.setKnowledgeName("历史演示知识");
        knowledge.setKnowledgeType("FAULT");
        knowledge.setKnowledgeContent("旧数据未记录创建人。");
        knowledge.setCreatedBy(null);
        return knowledge;
    }

    private KnowledgeSaveRequest validRequest() {
        KnowledgeSaveRequest request = new KnowledgeSaveRequest();
        request.setKnowledgeName("连接池耗尽风险知识");
        request.setKnowledgeContent("当连接使用率持续超过阈值时，建议排查长连接。");
        request.setKnowledgeType("FAULT");
        return request;
    }
}
