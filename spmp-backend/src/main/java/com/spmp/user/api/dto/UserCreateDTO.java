package com.spmp.user.api.dto;

import lombok.Data;

/**
 * 跨模块用户创建 DTO。
 * <p>
 * 供 owner 模块通过 UserApi 创建用户时使用。
 * 与 user 模块内部的 UserCreateDTO 不同，此 DTO 使用 roleCode 而非 roleIds。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class UserCreateDTO {

    /** 用户名（手机号） */
    private String username;

    /** 密码 */
    private String password;

    /** 角色编码（如 owner） */
    private String roleCode;
}
