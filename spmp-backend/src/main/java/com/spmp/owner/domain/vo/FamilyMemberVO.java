package com.spmp.owner.domain.vo;

import lombok.Data;

import java.util.Date;

/**
 * 家庭成员 VO（含脱敏字段）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class FamilyMemberVO {

    /** 成员ID */
    private Long id;

    /** 业主ID */
    private Long ownerId;

    /** 成员姓名 */
    private String memberName;

    /** 手机号（脱敏） */
    private String phoneMasked;

    /** 身份证号（脱敏） */
    private String idCardMasked;

    /** 与业主关系 */
    private String relation;

    /** 创建时间 */
    private Date createTime;
}
