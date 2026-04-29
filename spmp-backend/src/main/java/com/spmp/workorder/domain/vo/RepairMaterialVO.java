package com.spmp.workorder.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 维修材料 VO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class RepairMaterialVO {

    private Long id;

    private String materialName;

    private BigDecimal quantity;

    private String unit;

    private BigDecimal unitPrice;

    private BigDecimal totalPrice;
}
