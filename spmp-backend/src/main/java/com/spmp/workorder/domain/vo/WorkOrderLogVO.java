package com.spmp.workorder.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工单操作日志 VO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class WorkOrderLogVO {

    private Long id;

    private String action;

    private String actionDesc;

    private String fromStatus;

    private String toStatus;

    private Long operatorId;

    private String operatorName;

    private String operatorType;

    private String remark;

    private LocalDateTime operateTime;
}
