package com.spmp.workorder.domain.dto;

import lombok.Data;

/**
 * H5业主端我的工单查询 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class H5WorkOrderQueryDTO {

    private String status;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
