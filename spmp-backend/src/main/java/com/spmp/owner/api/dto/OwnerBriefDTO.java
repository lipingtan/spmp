package com.spmp.owner.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 业主简要信息 DTO（跨模块对外接口）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class OwnerBriefDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 业主ID */
    private Long id;

    /** 关联用户ID */
    private Long userId;

    /** 业主姓名 */
    private String ownerName;

    /** 手机号（脱敏后） */
    private String phoneMasked;

    /** 业主状态 */
    private String ownerStatus;
}
