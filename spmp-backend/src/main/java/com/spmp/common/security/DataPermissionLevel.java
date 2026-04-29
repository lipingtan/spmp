package com.spmp.common.security;

/**
 * 数据权限级别枚举。
 * <p>
 * 定义五级数据权限：全部 → 片区 → 小区 → 楼栋 → 仅本人。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public enum DataPermissionLevel {

    /** 全部数据 */
    ALL,

    /** 片区级 */
    AREA,

    /** 小区级 */
    COMMUNITY,

    /** 楼栋级 */
    BUILDING,

    /** 仅本人数据 */
    SELF
}
