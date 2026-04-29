package com.spmp.common.service.impl;

import com.spmp.common.exception.BusinessException;
import com.spmp.common.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 本地文件上传服务实现。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Value("${file.upload.path:/data/spmp/uploads}")
    private String uploadBasePath;

    @Value("${file.upload.max-size:5MB}")
    private DataSize maxFileSize;

    @Value("${file.upload.allowed-types:image/jpeg,image/png,image/gif,image/webp}")
    private String allowedTypesConfig;

    private static final List<String> IMAGE_MAGIC_BYTES = Arrays.asList(
            "FFD8FF",
            "89504E47",
            "47494638",
            "52494646"
    );

    @Override
    public String upload(MultipartFile file, String category) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(4001, "上传文件不能为空");
        }

        validateFileSize(file);
        validateFileType(file);
        validateMagicBytes(file);

        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;

        try {
            Path dirPath = Paths.get(uploadBasePath, category);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            Path filePath = dirPath.resolve(filename);
            file.transferTo(filePath.toFile());

            String url = "/api/v1/common/files/" + category + "/" + filename;
            log.info("文件上传成功：category={}, filename={}, size={}", category, filename, file.getSize());
            return url;
        } catch (IOException e) {
            log.error("文件上传失败：{}", e.getMessage(), e);
            throw new BusinessException(4002, "文件上传失败");
        }
    }

    @Override
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        try {
            String relativePath = fileUrl.replace("/api/v1/common/files/", "");
            Path filePath = Paths.get(uploadBasePath, relativePath);
            Files.deleteIfExists(filePath);
            log.info("文件删除成功：{}", fileUrl);
        } catch (IOException e) {
            log.warn("文件删除失败：{}", fileUrl, e);
        }
    }

    private void validateFileSize(MultipartFile file) {
        if (file.getSize() > maxFileSize.toBytes()) {
            throw new BusinessException(4003, "文件大小超过限制（最大" + maxFileSize.toMegabytes() + "MB）");
        }
    }

    private void validateFileType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new BusinessException(4004, "无法识别文件类型");
        }
        List<String> allowedTypes = Arrays.asList(allowedTypesConfig.split(","));
        if (!allowedTypes.contains(contentType)) {
            throw new BusinessException(4005, "不支持的文件类型：" + contentType);
        }
    }

    private void validateMagicBytes(MultipartFile file) {
        try {
            byte[] bytes = new byte[8];
            int read = file.getInputStream().read(bytes);
            if (read < 3) {
                return;
            }
            String hex = bytesToHex(bytes).toUpperCase();
            boolean valid = false;
            for (String magic : IMAGE_MAGIC_BYTES) {
                if (hex.startsWith(magic)) {
                    valid = true;
                    break;
                }
            }
            if (!valid) {
                throw new BusinessException(4005, "文件内容与文件类型不匹配");
            }
        } catch (IOException e) {
            log.warn("文件魔数校验失败：{}", e.getMessage());
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
