package com.spmp.billing.domain.dto;

import lombok.Data;

/**
 * H5 端账单查询 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class H5BillQueryDTO {

    private String status;

    private String feeType;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
