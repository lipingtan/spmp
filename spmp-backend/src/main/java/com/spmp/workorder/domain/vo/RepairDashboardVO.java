package com.spmp.workorder.domain.vo;

import lombok.Data;

/**
 * 维修人员工作台 VO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class RepairDashboardVO {

    private Integer todayPendingCount;

    private Integer monthlyCompletedCount;
}
