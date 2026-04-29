package com.spmp.workorder.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 派发记录 VO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class DispatchRecordVO {

    private Long id;

    private Long repairUserId;

    private String repairUserName;

    private String dispatchType;

    private String dispatcherName;

    private String remark;

    private LocalDateTime dispatchTime;
}
