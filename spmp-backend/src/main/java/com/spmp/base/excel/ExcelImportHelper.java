package com.spmp.base.excel;

import com.alibaba.excel.EasyExcel;
import com.spmp.base.constant.BaseConstants;
import com.spmp.base.constant.BaseErrorCode;
import com.spmp.base.domain.dto.ImportResultDTO;
import com.spmp.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Excel 导入公共辅助工具。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
public final class ExcelImportHelper {

    private ExcelImportHelper() {
    }

    /**
     * 校验上传文件。
     *
     * @param file 上传文件
     */
    public static void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(BaseErrorCode.IMPORT_FILE_EMPTY.getCode(),
                    BaseErrorCode.IMPORT_FILE_EMPTY.getMessage());
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new BusinessException(BaseErrorCode.IMPORT_FILE_FORMAT_ERROR.getCode(),
                    BaseErrorCode.IMPORT_FILE_FORMAT_ERROR.getMessage());
        }
    }

    /**
     * 构建导入结果。
     *
     * @param successCount 成功条数
     * @param errorList    错误列表
     * @param entityName   实体名称（用于错误报告文件名）
     * @return 导入结果
     */
    public static ImportResultDTO buildResult(int successCount, List<ExcelErrorInfo> errorList, String entityName) {
        ImportResultDTO result = new ImportResultDTO();
        result.setSuccessCount(successCount);
        result.setFailCount(errorList.size());
        result.setTotalCount(successCount + errorList.size());

        if (!errorList.isEmpty()) {
            try {
                String tempDir = System.getProperty("java.io.tmpdir");
                String errorFileName = entityName + "_import_errors_" + UUID.randomUUID().toString().substring(0, 8) + ".xlsx";
                String errorFilePath = tempDir + File.separator + errorFileName;
                EasyExcel.write(errorFilePath, ExcelErrorInfo.class).sheet("错误报告").doWrite(errorList);
                result.setErrorFileUrl("/tmp/" + errorFileName);
                log.info("生成{}导入错误报告: {}", entityName, errorFilePath);
            } catch (Exception e) {
                log.warn("生成{}导入错误报告失败: {}", entityName, e.getMessage());
            }
        }
        return result;
    }

    /**
     * 下载导入模板。
     *
     * @param response  HTTP 响应
     * @param clazz     Excel 模型类
     * @param sheetName 工作表名称
     * @param fileName  文件名
     */
    public static void downloadTemplate(HttpServletResponse response, Class<?> clazz,
                                        String sheetName, String fileName) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName);
            EasyExcel.write(response.getOutputStream(), clazz).sheet(sheetName).doWrite(Collections.emptyList());
        } catch (IOException e) {
            log.error("下载导入模板失败: {}", e.getMessage(), e);
            throw new BusinessException(BaseErrorCode.IMPORT_FILE_FORMAT_ERROR.getCode(), "模板下载失败");
        }
    }
}
