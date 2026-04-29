package com.spmp.dashboard.domain.dto;

import lombok.Data;

/**
 * Dashboard 查询参数。
 */
@Data
public class DashboardQueryDTO {

    /**
     * 时间范围：MONTH / QUARTER / YEAR
     */
    private String timeRange = "MONTH";
}

