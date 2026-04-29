package com.spmp.owner.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.spmp.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业主表实体。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ow_owner")
public class OwnerDO extends BaseEntity {

    /** 关联 sys_user.id，管理端录入时为空 */
    private Long userId;

    /** 业主姓名 */
    private String ownerName;

    /** 手机号（AES-256 加密存储） */
    private String phone;

    /** 身份证号（AES-256 加密存储） */
    private String idCard;

    /** 性别（0-未知 1-男 2-女） */
    private Integer gender;

    /** 头像URL */
    private String avatar;

    /** 邮箱 */
    private String email;

    /** 紧急联系人 */
    private String emergencyContact;

    /** 紧急联系电话（AES-256 加密存储） */
    private String emergencyPhone;

    /** 业主来源（ADMIN-管理端录入 H5-自助注册） */
    private String ownerSource;

    /** 业主状态（UNCERTIFIED-未认证 CERTIFYING-认证中 CERTIFIED-已认证 DISABLED-已停用） */
    private String ownerStatus;

    /** 停用前状态（启用时恢复用） */
    private String previousStatus;
}
