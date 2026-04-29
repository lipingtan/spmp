package com.spmp.owner.domain.vo;

import lombok.Data;

import java.util.Date;

/**
 * 业主列表 VO（含脱敏字段）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class OwnerListVO {

    /** 业主ID */
    private Long id;

    /** 业主姓名 */
    private String ownerName;

    /** 手机号（脱敏：138****5678） */
    private String phoneMasked;

    /** 身份证号（脱敏：110***********1234） */
    private String idCardMasked;

    /** 性别 */
    private Integer gender;

    /** 业主来源 */
    private String ownerSource;

    /** 业主状态 */
    private String ownerStatus;

    /** 首套房产所在小区（JOIN 查询） */
    private String communityName;

    /** 首套房产所在楼栋（JOIN 查询） */
    private String buildingName;

    /** 创建时间 */
    private Date createTime;
}
