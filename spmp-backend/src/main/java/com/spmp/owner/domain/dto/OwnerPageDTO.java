package com.spmp.owner.domain.dto;

import lombok.Data;

import java.util.Date;

/**
 * 业主分页查询结果 DTO（Mapper 返回用）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class OwnerPageDTO {

    /** 业主ID */
    private Long id;

    /** 业主姓名 */
    private String ownerName;

    /** 手机号（加密值） */
    private String phone;

    /** 身份证号（加密值） */
    private String idCard;

    /** 性别 */
    private Integer gender;

    /** 业主来源 */
    private String ownerSource;

    /** 业主状态 */
    private String ownerStatus;

    /** 创建时间 */
    private Date createTime;
}
