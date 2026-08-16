package com.xinchuang.diagnosis.ai.service.impl;

import com.xinchuang.diagnosis.ai.entity.KnowledgeAttachment;
import com.xinchuang.diagnosis.ai.service.KnowledgeFileStorageService;
import com.xinchuang.diagnosis.common.exception.BusinessException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalKnowledgeFileStorageService implements KnowledgeFileStorageService {
    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        "jpg", "jpeg", "png", "gif", "webp", "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "md", "zip", "rar", "7z"
    );
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private final Path localRoot;

    public LocalKnowledgeFileStorageService(@Value("${storage.knowledge.local-root}") String localRoot) {
        this.localRoot = Path.of(localRoot).toAbsolutePath().normalize();
    }

    @Override
    public KnowledgeAttachment store(Long knowledgeId, MultipartFile file, Long operatorId) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("附件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("单个附件不能超过20MB");
        }
        String originalFilename = Path.of(StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "attachment").getFileName().toString();
        String extension = extensionOf(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("不支持的附件格式：" + extension);
        }
        String relativeDirectory = LocalDate.now().toString().replace("-", "/") + "/" + knowledgeId;
        String storedFilename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path destination = localRoot.resolve(relativeDirectory).resolve(storedFilename).normalize();
        if (!destination.startsWith(localRoot)) {
            throw new BusinessException("非法附件路径");
        }
        try {
            Files.createDirectories(destination.getParent());
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            if (IMAGE_EXTENSIONS.contains(extension)) {
                BufferedImage image = ImageIO.read(destination.toFile());
                if (image == null) {
                    Files.deleteIfExists(destination);
                    throw new BusinessException("图片附件内容无效");
                }
            }
        } catch (IOException ex) {
            throw new BusinessException("附件保存失败");
        }
        KnowledgeAttachment attachment = new KnowledgeAttachment();
        attachment.setKnowledgeId(knowledgeId);
        attachment.setOriginalFilename(originalFilename);
        attachment.setStoredFilename(storedFilename);
        attachment.setStoragePath(relativeDirectory + "/" + storedFilename);
        attachment.setContentType(StringUtils.hasText(file.getContentType()) ? file.getContentType() : "application/octet-stream");
        attachment.setFileExtension(extension);
        attachment.setFileSize(file.getSize());
        attachment.setCreatedBy(operatorId);
        attachment.setUpdatedBy(operatorId);
        return attachment;
    }

    @Override
    public Path resolve(KnowledgeAttachment attachment) {
        Path path = localRoot.resolve(attachment.getStoragePath()).normalize();
        if (!path.startsWith(localRoot) || !Files.isRegularFile(path)) {
            throw new BusinessException("附件物理文件不存在");
        }
        return path;
    }

    @Override
    public boolean isPreviewable(KnowledgeAttachment attachment) {
        return IMAGE_EXTENSIONS.contains(attachment.getFileExtension());
    }

    private String extensionOf(String filename) {
        int index = filename.lastIndexOf('.');
        if (index <= 0 || index == filename.length() - 1) {
            throw new BusinessException("附件必须带有文件扩展名");
        }
        return filename.substring(index + 1).toLowerCase(Locale.ROOT);
    }
}
