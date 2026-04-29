package com.spmp.billing.domain.dto;

import lombok.Data;

/**
 * PC端账单列表查询 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class BillQueryDTO {

    private String status;

    private String feeType;

    private Long communityId;

    private Long buildingId;

    private String billingPeriod;

    private String keyword;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
