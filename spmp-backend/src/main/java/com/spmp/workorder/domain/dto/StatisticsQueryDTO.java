package com.spmp.workorder.domain.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 统计查询 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class StatisticsQueryDTO {

    private String timeRange;

    private LocalDate startDate;

    private LocalDate endDate;

    private Long communityId;

    private Long buildingId;
}
