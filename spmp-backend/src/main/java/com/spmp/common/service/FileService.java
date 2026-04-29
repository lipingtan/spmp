package com.spmp.common.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 统一文件上传服务接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface FileService {

    /**
     * 上传文件，返回文件访问 URL。
     *
     * @param file     文件
     * @param category 分类目录（如 workorder、avatar）
     * @return 文件访问 URL
     */
    String upload(MultipartFile file, String category);

    /**
     * 删除文件。
     *
     * @param fileUrl 文件 URL
     */
    void delete(String fileUrl);
}
