package com.spmp.workorder.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * PC端工单列表 VO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class WorkOrderListVO {

    private Long id;

    private String orderNo;

    private String orderType;

    private String addressType;

    private Long communityId;

    private String reporterName;

    private String status;

    private Long repairUserId;

    private String repairUserName;

    private Integer rejectCount;

    private Integer urgeCount;

    private LocalDateTime createTime;

    private LocalDateTime actualCompleteTime;

    private Integer repairDuration;
}
