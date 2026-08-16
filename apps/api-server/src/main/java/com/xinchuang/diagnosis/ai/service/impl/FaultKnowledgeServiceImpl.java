package com.xinchuang.diagnosis.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinchuang.diagnosis.ai.dto.KnowledgePageQuery;
import com.xinchuang.diagnosis.ai.dto.KnowledgeSaveRequest;
import com.xinchuang.diagnosis.ai.entity.KnowledgeAttachment;
import com.xinchuang.diagnosis.ai.entity.KnowledgeInstance;
import com.xinchuang.diagnosis.ai.mapper.KnowledgeAttachmentMapper;
import com.xinchuang.diagnosis.ai.mapper.KnowledgeInstanceMapper;
import com.xinchuang.diagnosis.ai.service.FaultKnowledgeService;
import com.xinchuang.diagnosis.ai.service.KnowledgeFileStorageService;
import com.xinchuang.diagnosis.ai.vo.KnowledgeAttachmentVO;
import com.xinchuang.diagnosis.ai.vo.KnowledgeVO;
import com.xinchuang.diagnosis.common.api.PageResponse;
import com.xinchuang.diagnosis.common.exception.BusinessException;
import com.xinchuang.diagnosis.security.LoginUser;
import com.xinchuang.diagnosis.security.UserContext;
import com.xinchuang.diagnosis.system.entity.SysUser;
import com.xinchuang.diagnosis.system.mapper.SysUserMapper;
import com.xinchuang.diagnosis.system.service.AuditLogService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FaultKnowledgeServiceImpl implements FaultKnowledgeService {
    private static final int MAX_ATTACHMENTS = 10;
    private final KnowledgeInstanceMapper knowledgeMapper;
    private final KnowledgeAttachmentMapper attachmentMapper;
    private final KnowledgeFileStorageService fileStorageService;
    private final SysUserMapper userMapper;
    private final AuditLogService auditLogService;

    public FaultKnowledgeServiceImpl(KnowledgeInstanceMapper knowledgeMapper,
                                     KnowledgeAttachmentMapper attachmentMapper,
                                     KnowledgeFileStorageService fileStorageService,
                                     SysUserMapper userMapper,
                                     AuditLogService auditLogService) {
        this.knowledgeMapper = knowledgeMapper;
        this.attachmentMapper = attachmentMapper;
        this.fileStorageService = fileStorageService;
        this.userMapper = userMapper;
        this.auditLogService = auditLogService;
    }

    @Override
    public PageResponse<KnowledgeVO> page(KnowledgePageQuery query) {
        LambdaQueryWrapper<KnowledgeInstance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeInstance::getKnowledgeType, normalizeType(query.getKnowledgeType()));
        if (StringUtils.hasText(query.getKnowledgeId())) {
            wrapper.like(KnowledgeInstance::getKnowledgeCode, query.getKnowledgeId().trim());
        }
        if (StringUtils.hasText(query.getKnowledgeName())) {
            wrapper.like(KnowledgeInstance::getKnowledgeName, query.getKnowledgeName().trim());
        }
        if (query.getCreatedDate() != null) {
            LocalDateTime start = query.getCreatedDate().atStartOfDay();
            wrapper.ge(KnowledgeInstance::getCreatedAt, start).lt(KnowledgeInstance::getCreatedAt, start.plusDays(1));
        }
        if (StringUtils.hasText(query.getCreatorName())) {
            List<Long> creatorIds = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                    .and(item -> item.like(SysUser::getUsername, query.getCreatorName().trim())
                        .or().like(SysUser::getRealName, query.getCreatorName().trim())))
                .stream().map(SysUser::getId).toList();
            if (creatorIds.isEmpty()) {
                return PageResponse.of(List.of(), 0, query.getPageNo(), query.getPageSize());
            }
            wrapper.in(KnowledgeInstance::getCreatedBy, creatorIds);
        }
        wrapper.orderByDesc(KnowledgeInstance::getUpdatedAt);
        Page<KnowledgeInstance> page = knowledgeMapper.selectPage(new Page<>(query.getPageNo(), query.getPageSize()), wrapper);
        Map<Long, String> creators = creatorNames(page.getRecords());
        return PageResponse.of(page.getRecords().stream()
                .map(item -> toVO(item, creatorName(creators, item.getCreatedBy()), List.of()))
                .toList(),
            page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public KnowledgeVO detail(Long id) {
        KnowledgeInstance knowledge = requiredKnowledge(id);
        List<KnowledgeAttachment> attachments = activeAttachments(id);
        return toVO(knowledge, creatorName(creatorNames(List.of(knowledge)), knowledge.getCreatedBy()), attachments);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized KnowledgeVO create(KnowledgeSaveRequest request, List<MultipartFile> files, boolean requireAttachment) {
        validateRequest(request);
        validateAttachments(files, List.of(), requireAttachment);
        LoginUser user = requiredUser();
        KnowledgeInstance knowledge = new KnowledgeInstance();
        applyRequest(knowledge, request, null);
        String knowledgeType = normalizeType(request.getKnowledgeType());
        knowledge.setKnowledgeCode(StringUtils.hasText(request.getKnowledgeCode()) ? request.getKnowledgeCode().trim() : nextKnowledgeCode(knowledgeType));
        ensureCodeUnique(knowledge.getKnowledgeCode(), null);
        ensureNameUnique(knowledge.getKnowledgeName(), null);
        knowledge.setKnowledgeType(knowledgeType);
        knowledge.setSourceType("MANUAL");
        knowledge.setStatus("ENABLED");
        knowledge.setVersion(1);
        knowledge.setCreatedBy(user.getUserId());
        knowledge.setUpdatedBy(user.getUserId());
        knowledgeMapper.insert(knowledge);
        saveFiles(knowledge, files, user.getUserId());
        refreshLegacyAttachmentNames(knowledge, user.getUserId());
        auditLogService.recordSuccess(knowledgeLabel(knowledgeType), "CREATE", "KB_INSTANCE", knowledge.getId(), "新增" + knowledgeLabel(knowledgeType) + "：" + knowledge.getKnowledgeName());
        return detail(knowledge.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized KnowledgeVO update(Long id, KnowledgeSaveRequest request, List<MultipartFile> files, boolean requireAttachment) {
        validateRequest(request);
        KnowledgeInstance knowledge = requiredKnowledge(id);
        List<KnowledgeAttachment> existing = activeAttachments(id);
        List<Long> retainedIds = request.getRetainedAttachmentIds();
        validateOwnedAttachmentIds(existing, retainedIds);
        validateAttachments(files, retainedIds, requireAttachment);
        LoginUser user = requiredUser();
        applyRequest(knowledge, request, id);
        ensureNameUnique(knowledge.getKnowledgeName(), id);
        knowledge.setUpdatedBy(user.getUserId());
        knowledgeMapper.updateById(knowledge);
        existing.stream().filter(item -> !retainedIds.contains(item.getId())).forEach(attachmentMapper::deleteById);
        saveFiles(knowledge, files, user.getUserId());
        refreshLegacyAttachmentNames(knowledge, user.getUserId());
        auditLogService.recordSuccess(knowledgeLabel(knowledge.getKnowledgeType()), "UPDATE", "KB_INSTANCE", id, "编辑" + knowledgeLabel(knowledge.getKnowledgeType()) + "：" + knowledge.getKnowledgeName());
        return detail(id);
    }

    @Override
    public KnowledgeVO createLegacy(KnowledgeInstance request) {
        KnowledgeSaveRequest command = fromLegacy(request);
        return create(command, List.of(), false);
    }

    @Override
    public KnowledgeVO updateLegacy(Long id, KnowledgeInstance request) {
        KnowledgeSaveRequest command = fromLegacy(request);
        command.setRetainedAttachmentIds(activeAttachments(id).stream().map(KnowledgeAttachment::getId).toList());
        return update(id, command, List.of(), false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        KnowledgeInstance knowledge = requiredKnowledge(id);
        activeAttachments(id).forEach(attachmentMapper::deleteById);
        knowledgeMapper.deleteById(id);
        auditLogService.recordSuccess(knowledgeLabel(knowledge.getKnowledgeType()), "DELETE", "KB_INSTANCE", id, "逻辑删除" + knowledgeLabel(knowledge.getKnowledgeType()) + "：" + knowledge.getKnowledgeName());
    }

    @Override
    public KnowledgeAttachment attachmentDetail(Long attachmentId) {
        KnowledgeAttachment attachment = attachmentMapper.selectById(attachmentId);
        if (attachment == null || attachment.getDeleted() != null && attachment.getDeleted() == 1) {
            throw new BusinessException("附件不存在或已删除");
        }
        requiredKnowledge(attachment.getKnowledgeId());
        return attachment;
    }

    private void saveFiles(KnowledgeInstance knowledge, List<MultipartFile> files, Long userId) {
        if (files == null) return;
        for (MultipartFile file : files) {
            KnowledgeAttachment attachment = fileStorageService.store(knowledge.getId(), file, userId);
            attachmentMapper.insert(attachment);
        }
    }

    private void refreshLegacyAttachmentNames(KnowledgeInstance knowledge, Long userId) {
        List<KnowledgeAttachment> attachments = activeAttachments(knowledge.getId());
        knowledge.setAttachmentNames(attachments.stream().map(KnowledgeAttachment::getOriginalFilename).collect(Collectors.joining(",")));
        knowledge.setUpdatedBy(userId);
        knowledgeMapper.updateById(knowledge);
    }

    private void validateRequest(KnowledgeSaveRequest request) {
        if (request == null || !StringUtils.hasText(request.getKnowledgeName())) throw new BusinessException("知识名称不能为空");
        if (!StringUtils.hasText(request.getKnowledgeContent())) throw new BusinessException("知识内容不能为空");
        if (!StringUtils.hasText(request.getKnowledgeType())) throw new BusinessException("知识类型不能为空");
        if (request.getKnowledgeName().trim().length() > 150) throw new BusinessException("知识名称不能超过150个字符");
        if (!"FAULT".equals(normalizeType(request.getKnowledgeType())) && !"DEFECT".equals(normalizeType(request.getKnowledgeType())) && !"OPTIMIZATION".equals(normalizeType(request.getKnowledgeType()))) {
            throw new BusinessException("知识类型不合法");
        }
    }

    private void validateAttachments(List<MultipartFile> files, List<Long> retainedIds, boolean requireAttachment) {
        int newCount = files == null ? 0 : (int) files.stream().filter(Objects::nonNull).count();
        int total = retainedIds.size() + newCount;
        if (requireAttachment && total == 0) throw new BusinessException("请至少上传一个附件");
        if (total > MAX_ATTACHMENTS) throw new BusinessException("每条知识最多上传10个附件");
    }

    private void validateOwnedAttachmentIds(List<KnowledgeAttachment> existing, List<Long> retainedIds) {
        Set<Long> existingIds = existing.stream().map(KnowledgeAttachment::getId).collect(Collectors.toSet());
        if (retainedIds.size() != retainedIds.stream().distinct().count() || !existingIds.containsAll(retainedIds)) {
            throw new BusinessException("附件清单包含无效记录");
        }
    }

    private void applyRequest(KnowledgeInstance target, KnowledgeSaveRequest request, Long id) {
        target.setKnowledgeName(request.getKnowledgeName().trim());
        target.setKnowledgeContent(request.getKnowledgeContent().trim());
        target.setKnowledgeType(normalizeType(request.getKnowledgeType()));
        if (id == null && StringUtils.hasText(request.getKnowledgeCode())) target.setKnowledgeCode(request.getKnowledgeCode().trim());
    }

    private String normalizeType(String value) { return value == null ? "" : value.trim().toUpperCase(); }

    private KnowledgeInstance requiredKnowledge(Long id) {
        KnowledgeInstance knowledge = knowledgeMapper.selectById(id);
        if (knowledge == null) throw new BusinessException("知识不存在或已删除");
        return knowledge;
    }

    private List<KnowledgeAttachment> activeAttachments(Long knowledgeId) {
        return attachmentMapper.selectList(new LambdaQueryWrapper<KnowledgeAttachment>().eq(KnowledgeAttachment::getKnowledgeId, knowledgeId)
            .orderByAsc(KnowledgeAttachment::getCreatedAt));
    }

    private Map<Long, String> creatorNames(Collection<KnowledgeInstance> knowledge) {
        Set<Long> ids = knowledge.stream().map(KnowledgeInstance::getCreatedBy).filter(Objects::nonNull).collect(Collectors.toSet());
        if (ids.isEmpty()) return Map.of();
        return userMapper.selectBatchIds(ids).stream().collect(Collectors.toMap(SysUser::getId,
            user -> StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUsername()));
    }

    private String creatorName(Map<Long, String> creators, Long creatorId) {
        return creatorId == null ? null : creators.get(creatorId);
    }

    private KnowledgeVO toVO(KnowledgeInstance entity, String creatorName, List<KnowledgeAttachment> attachments) {
        KnowledgeVO vo = new KnowledgeVO();
        vo.setId(entity.getId()); vo.setKnowledgeCode(entity.getKnowledgeCode()); vo.setKnowledgeName(entity.getKnowledgeName());
        vo.setKnowledgeType(entity.getKnowledgeType()); vo.setKnowledgeContent(entity.getKnowledgeContent()); vo.setCreatorName(creatorName);
        vo.setCreatedAt(entity.getCreatedAt()); vo.setUpdatedAt(entity.getUpdatedAt());
        vo.setAttachments(attachments.stream().map(item -> { KnowledgeAttachmentVO attachment = new KnowledgeAttachmentVO();
            attachment.setId(item.getId()); attachment.setOriginalFilename(item.getOriginalFilename()); attachment.setContentType(item.getContentType());
            attachment.setFileSize(item.getFileSize()); attachment.setPreviewable(fileStorageService.isPreviewable(item)); return attachment; }).toList());
        return vo;
    }

    private void ensureCodeUnique(String code, Long currentId) {
        Long count = knowledgeMapper.selectCount(new LambdaQueryWrapper<KnowledgeInstance>().eq(KnowledgeInstance::getKnowledgeCode, code)
            .ne(currentId != null, KnowledgeInstance::getId, currentId));
        if (count != null && count > 0) throw new BusinessException("知识编码已存在");
    }

    private void ensureNameUnique(String name, Long currentId) {
        Long count = knowledgeMapper.selectCount(new LambdaQueryWrapper<KnowledgeInstance>().eq(KnowledgeInstance::getKnowledgeName, name)
            .ne(currentId != null, KnowledgeInstance::getId, currentId));
        if (count != null && count > 0) throw new BusinessException("知识名称已存在");
    }

    private String nextKnowledgeCode(String knowledgeType) {
        String prefix = switch (normalizeType(knowledgeType)) {
            case "DEFECT" -> "DEFECT-KB-";
            case "OPTIMIZATION" -> "OPT-KB-";
            default -> "FAULT-KB-";
        };
        return prefix + System.currentTimeMillis();
    }

    private String knowledgeLabel(String knowledgeType) {
        return switch (normalizeType(knowledgeType)) {
            case "DEFECT" -> "缺陷知识库";
            case "OPTIMIZATION" -> "优化知识库";
            default -> "故障知识库";
        };
    }
    private LoginUser requiredUser() { LoginUser user = UserContext.get(); if (user == null) throw new BusinessException(401, "登录状态已失效"); return user; }
    private KnowledgeSaveRequest fromLegacy(KnowledgeInstance request) {
        KnowledgeSaveRequest command = new KnowledgeSaveRequest();
        command.setKnowledgeCode(request.getKnowledgeCode()); command.setKnowledgeName(request.getKnowledgeName()); command.setKnowledgeType(request.getKnowledgeType()); command.setKnowledgeContent(request.getKnowledgeContent());
        return command;
    }
}
