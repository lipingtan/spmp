package com.spmp.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.spmp.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户表实体。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class UserDO extends BaseEntity {

    /** 用户名 */
    private String username;

    /** 密码（BCrypt 加密） */
    private String password;

    /** 姓名 */
    private String realName;

    /** 手机号（AES 加密） */
    private String phone;

    /** 手机号 SHA-256 哈希 */
    private String phoneHash;

    /** 头像 URL */
    private String avatar;

    /** 状态（0-启用 1-禁用） */
    private Integer status;
}
