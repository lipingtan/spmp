package com.spmp.workorder.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 业主工单列表 DTO（对外 API）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class WorkOrderOwnerDTO {

    private Long id;

    private String orderNo;

    private String orderType;

    private String status;

    private String description;

    private LocalDateTime createTime;
}
