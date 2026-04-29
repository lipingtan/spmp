package com.spmp.billing.domain.vo;

import lombok.Data;

/**
 * 批量生成结果 VO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class BillGenerateResultVO {

    private String batchNo;
    private int total;
    private int success;
    private int skipped;
    private int failed;
    private String status;
}
