package com.spmp.workorder.domain.vo;

import lombok.Data;

/**
 * 维修人员选项 VO（用于派发下拉选择）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class RepairStaffVO {

    private Long userId;

    private String realName;

    private String phone;

    private Integer currentWorkload;
}
