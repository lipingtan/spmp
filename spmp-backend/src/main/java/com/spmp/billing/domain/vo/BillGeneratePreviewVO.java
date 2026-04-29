package com.spmp.billing.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 批量生成预览 VO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class BillGeneratePreviewVO {

    private int totalHouses;
    private int willGenerate;
    private int skippedVacant;
    private int skippedDisabled;
    private int skippedNoConfig;
    private BigDecimal totalAmount;
}
