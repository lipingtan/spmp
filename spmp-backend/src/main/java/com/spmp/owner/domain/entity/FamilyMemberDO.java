package com.spmp.owner.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.spmp.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 家庭成员表实体。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ow_family_member")
public class FamilyMemberDO extends BaseEntity {

    /** 业主ID */
    private Long ownerId;

    /** 关联 sys_user.id */
    private Long userId;

    /** 成员姓名 */
    private String memberName;

    /** 手机号（AES-256 加密存储） */
    private String phone;

    /** 身份证号（AES-256 加密存储） */
    private String idCard;

    /** 与业主关系（SPOUSE-配偶 PARENT-父母 CHILD-子女 OTHER-其他） */
    private String relation;
}
