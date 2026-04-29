package com.spmp.base.domain.dto;

import lombok.Data;

/**
 * Excel 导入结果 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class ImportResultDTO {

    /** 总条数 */
    private Integer totalCount;

    /** 成功条数 */
    private Integer successCount;

    /** 失败条数 */
    private Integer failCount;

    /** 错误报告下载URL（如有失败记录） */
    private String errorFileUrl;
}
