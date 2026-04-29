package com.spmp.user.constant;

/**
 * 用户模块常量定义。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public final class UserConstants {

    private UserConstants() {
    }

    /** 默认密码 */
    public static final String DEFAULT_PASSWORD = "Spmp@2026";

    /** 超级管理员角色编码 */
    public static final String SUPER_ADMIN_ROLE_CODE = "super_admin";

    /** 超级管理员用户 ID */
    public static final Long SUPER_ADMIN_USER_ID = 1L;

    /** 最大登录失败次数 */
    public static final int MAX_LOGIN_FAIL_COUNT = 5;

    /** 账号锁定时间（分钟） */
    public static final int LOCK_MINUTES = 30;

    /** 验证码有效期（秒） */
    public static final int CAPTCHA_EXPIRE_SECONDS = 120;

    /** 短信验证码有效期（秒） */
    public static final int SMS_CODE_EXPIRE_SECONDS = 300;

    /** 短信发送间隔（秒） */
    public static final int SMS_INTERVAL_SECONDS = 60;

    /** 短信每日发送上限 */
    public static final int SMS_DAILY_LIMIT = 10;

    // ========== Redis Key 前缀 ==========

    /** 登录失败计数 */
    public static final String LOGIN_FAIL_KEY = "login:fail:";

    /** 账号锁定 */
    public static final String LOGIN_LOCK_KEY = "login:lock:";

    /** 图形验证码 */
    public static final String CAPTCHA_KEY = "captcha:";

    /** 短信验证码 */
    public static final String SMS_CODE_KEY = "sms:code:";

    /** 短信发送间隔 */
    public static final String SMS_INTERVAL_KEY = "sms:interval:";

    /** 短信每日计数 */
    public static final String SMS_DAILY_KEY = "sms:daily:";

    /** Token 黑名单 */
    public static final String TOKEN_BLACKLIST_KEY = "token:blacklist:";

    /** 用户权限缓存 */
    public static final String USER_PERMISSIONS_KEY = "user:permissions:";

    /** 用户数据权限缓存 */
    public static final String USER_DATA_PERMISSION_KEY = "user:data-permission:";

    /** 用户菜单缓存 */
    public static final String USER_MENUS_KEY = "user:menus:";

    // ========== 用户状态 ==========

    /** 启用 */
    public static final int STATUS_ENABLED = 0;

    /** 禁用 */
    public static final int STATUS_DISABLED = 1;

    /** 密码强度正则：至少8位，包含字母和数字 */
    public static final String PASSWORD_PATTERN = "^(?=.*[a-zA-Z])(?=.*\\d).{8,}$";
}
