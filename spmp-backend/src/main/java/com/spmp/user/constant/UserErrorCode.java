package com.spmp.user.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户模块错误码枚举（2000-2999）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum UserErrorCode {

    // ========== 认证相关 2000-2099 ==========
    USER_NOT_FOUND(2001, "用户名或密码错误"),
    PASSWORD_ERROR(2002, "用户名或密码错误"),
    ACCOUNT_LOCKED(2003, "账号已锁定，请稍后重试"),
    ACCOUNT_DISABLED(2004, "账号已被禁用"),
    CAPTCHA_ERROR(2005, "验证码错误"),
    CAPTCHA_EXPIRED(2006, "验证码已过期"),
    SMS_CODE_ERROR(2007, "验证码错误或已过期"),
    PHONE_NOT_REGISTERED(2008, "该手机号未注册"),
    SMS_SEND_TOO_FREQUENT(2009, "请稍后再试"),
    SMS_DAILY_LIMIT(2010, "今日发送次数已达上限"),
    TOKEN_INVALID(2011, "令牌无效，请重新登录"),
    TOKEN_EXPIRED(2012, "令牌已过期，请重新登录"),

    // ========== 用户管理 2100-2199 ==========
    USERNAME_EXISTS(2101, "用户名已存在"),
    PHONE_EXISTS(2102, "手机号已存在"),
    CANNOT_DELETE_ADMIN(2103, "不能删除超级管理员"),
    CANNOT_DISABLE_SELF(2104, "不能禁用自己的账号"),
    CANNOT_DELETE_SELF(2105, "不能删除自己的账号"),
    CANNOT_DISABLE_ADMIN(2106, "不能禁用超级管理员"),

    // ========== 角色管理 2200-2299 ==========
    ROLE_CODE_EXISTS(2201, "角色编码已存在"),
    ROLE_NAME_EXISTS(2202, "角色名称已存在"),
    ROLE_HAS_USERS(2203, "该角色下存在用户，无法删除"),
    CANNOT_MODIFY_ADMIN_ROLE(2204, "不能修改超级管理员角色"),
    CANNOT_DELETE_ADMIN_ROLE(2205, "不能删除超级管理员角色"),

    // ========== 菜单管理 2300-2399 ==========
    MENU_HAS_CHILDREN(2301, "存在子菜单，无法删除"),
    MENU_USED_BY_ROLE(2302, "该菜单已被角色使用，无法删除"),
    MENU_PARENT_INVALID(2303, "父级菜单无效"),
    MENU_NAME_DUPLICATE(2304, "同一父级下菜单名称重复"),

    // ========== 个人中心 2400-2499 ==========
    OLD_PASSWORD_ERROR(2401, "旧密码错误"),
    NEW_PASSWORD_SAME(2402, "新密码不能与旧密码相同"),
    PASSWORD_TOO_WEAK(2403, "密码强度不够，至少8位且包含字母和数字"),

    // ========== 限流 2900-2999 ==========
    RATE_LIMIT_EXCEEDED(2901, "请求过于频繁，请稍后重试");

    private final int code;
    private final String message;
}
