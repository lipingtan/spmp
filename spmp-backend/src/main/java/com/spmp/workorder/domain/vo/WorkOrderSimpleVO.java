package com.spmp.workorder.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * H5端工单列表 VO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class WorkOrderSimpleVO {

    private Long id;

    private String orderNo;

    private String orderType;

    private String status;

    private String description;

    private String addressDesc;

    private LocalDateTime createTime;

    private LocalDateTime actualCompleteTime;
}
