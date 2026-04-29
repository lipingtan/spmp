package com.spmp.workorder.domain.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * PC端工单列表查询 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class WorkOrderQueryDTO {

    private String status;

    private String orderType;

    private Long communityId;

    private Long buildingId;

    private LocalDate startDate;

    private LocalDate endDate;

    private String keyword;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
