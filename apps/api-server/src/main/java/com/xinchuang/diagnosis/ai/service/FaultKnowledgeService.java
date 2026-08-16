package com.xinchuang.diagnosis.ai.service;

import com.xinchuang.diagnosis.ai.dto.KnowledgePageQuery;
import com.xinchuang.diagnosis.ai.dto.KnowledgeSaveRequest;
import com.xinchuang.diagnosis.ai.entity.KnowledgeAttachment;
import com.xinchuang.diagnosis.ai.entity.KnowledgeInstance;
import com.xinchuang.diagnosis.ai.vo.KnowledgeVO;
import com.xinchuang.diagnosis.common.api.PageResponse;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface FaultKnowledgeService {
    PageResponse<KnowledgeVO> page(KnowledgePageQuery query);
    KnowledgeVO detail(Long id);
    KnowledgeVO create(KnowledgeSaveRequest request, List<MultipartFile> files, boolean requireAttachment);
    KnowledgeVO update(Long id, KnowledgeSaveRequest request, List<MultipartFile> files, boolean requireAttachment);
    KnowledgeVO createLegacy(KnowledgeInstance request);
    KnowledgeVO updateLegacy(Long id, KnowledgeInstance request);
    void delete(Long id);
    KnowledgeAttachment attachmentDetail(Long attachmentId);
}
