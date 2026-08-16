package com.xinchuang.diagnosis.ai.controller;

import com.xinchuang.diagnosis.ai.dto.KnowledgePageQuery;
import com.xinchuang.diagnosis.ai.dto.KnowledgeSaveRequest;
import com.xinchuang.diagnosis.ai.entity.KnowledgeAttachment;
import com.xinchuang.diagnosis.ai.entity.KnowledgeInstance;
import com.xinchuang.diagnosis.ai.service.FaultKnowledgeService;
import com.xinchuang.diagnosis.ai.service.KnowledgeFileStorageService;
import com.xinchuang.diagnosis.ai.vo.KnowledgeVO;
import com.xinchuang.diagnosis.common.api.ApiResponse;
import com.xinchuang.diagnosis.common.api.PageResponse;
import com.xinchuang.diagnosis.common.exception.BusinessException;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/ai/knowledge")
public class AiKnowledgeController {
    private final FaultKnowledgeService knowledgeService;
    private final KnowledgeFileStorageService fileStorageService;

    public AiKnowledgeController(FaultKnowledgeService knowledgeService, KnowledgeFileStorageService fileStorageService) {
        this.knowledgeService = knowledgeService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public ApiResponse<PageResponse<KnowledgeVO>> page(@Valid @ModelAttribute KnowledgePageQuery query) {
        return ApiResponse.ok(knowledgeService.page(query));
    }

    @GetMapping("/{id}")
    public ApiResponse<KnowledgeVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(knowledgeService.detail(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<KnowledgeVO> createLegacy(@RequestBody KnowledgeInstance request) {
        return ApiResponse.ok(knowledgeService.createLegacy(request));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<KnowledgeVO> create(@ModelAttribute KnowledgeSaveRequest request,
                                            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return ApiResponse.ok(knowledgeService.create(request, files, true));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<KnowledgeVO> updateLegacy(@PathVariable Long id, @RequestBody KnowledgeInstance request) {
        return ApiResponse.ok(knowledgeService.updateLegacy(id, request));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<KnowledgeVO> update(@PathVariable Long id, @ModelAttribute KnowledgeSaveRequest request,
                                            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return ApiResponse.ok(knowledgeService.update(id, request, files, true));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        knowledgeService.delete(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/attachments/{attachmentId}/download")
    public ResponseEntity<FileSystemResource> download(@PathVariable Long attachmentId) {
        KnowledgeAttachment attachment = knowledgeService.attachmentDetail(attachmentId);
        Path file = fileStorageService.resolve(attachment);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .contentLength(attachment.getFileSize())
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(attachment.getOriginalFilename(), StandardCharsets.UTF_8).build().toString())
            .body(new FileSystemResource(file));
    }

    @GetMapping("/attachments/{attachmentId}/preview")
    public ResponseEntity<FileSystemResource> preview(@PathVariable Long attachmentId) {
        KnowledgeAttachment attachment = knowledgeService.attachmentDetail(attachmentId);
        if (!fileStorageService.isPreviewable(attachment)) {
            throw new BusinessException("仅图片附件支持预览");
        }
        Path file = fileStorageService.resolve(attachment);
        return ResponseEntity.ok().contentType(imageMediaType(attachment.getFileExtension()))
            .contentLength(attachment.getFileSize()).body(new FileSystemResource(file));
    }

    private MediaType imageMediaType(String extension) {
        return switch (extension) {
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "png" -> MediaType.IMAGE_PNG;
            case "gif" -> MediaType.IMAGE_GIF;
            case "webp" -> MediaType.parseMediaType("image/webp");
            default -> throw new BusinessException("仅图片附件支持预览");
        };
    }
}
