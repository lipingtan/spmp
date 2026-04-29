package com.spmp.workorder.api.dto;

import lombok.Data;

/**
 * 按小区统计工单数 DTO（对外 API）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class WorkOrderStatisticsDTO {

    private Integer totalCount;

    private Integer pendingCount;

    private Integer inProgressCount;

    private Integer completedCount;
}
