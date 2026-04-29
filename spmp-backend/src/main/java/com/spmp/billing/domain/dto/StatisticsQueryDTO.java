package com.spmp.billing.domain.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 收费统计查询 DTO。
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

    private String feeType;
}
