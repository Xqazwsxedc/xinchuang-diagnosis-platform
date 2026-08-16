package com.xinchuang.diagnosis.ai.service;

import com.xinchuang.diagnosis.ai.entity.KnowledgeAttachment;
import java.nio.file.Path;
import org.springframework.web.multipart.MultipartFile;

public interface KnowledgeFileStorageService {
    KnowledgeAttachment store(Long knowledgeId, MultipartFile file, Long operatorId);
    Path resolve(KnowledgeAttachment attachment);
    boolean isPreviewable(KnowledgeAttachment attachment);
}
