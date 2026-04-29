package com.spmp.workorder.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工单摘要信息 DTO（对外 API）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class WorkOrderBriefDTO {

    private Long id;

    private String orderNo;

    private String orderType;

    private String status;

    private String description;

    private String reporterName;

    private LocalDateTime createTime;

    private LocalDateTime actualCompleteTime;
}
