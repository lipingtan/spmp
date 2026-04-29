package com.spmp.owner.constant;

/**
 * 业主管理模块常量定义。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public final class OwnerConstants {

    private OwnerConstants() {
    }

    // ========== Redis Key 前缀 ==========

    /** 按 ID 查业主缓存 key 前缀 */
    public static final String CACHE_OWNER_ID_KEY = "spmp:owner:id:";

    /** 按房屋查业主缓存 key 前缀 */
    public static final String CACHE_OWNER_HOUSE_KEY = "spmp:owner:house:";

    /** 按手机号查业主缓存 key 前缀 */
    public static final String CACHE_OWNER_PHONE_KEY = "spmp:owner:phone:";

    /** 按小区查业主列表缓存 key 前缀 */
    public static final String CACHE_OWNER_COMMUNITY_KEY = "spmp:owner:community:";

    /** 按楼栋查业主列表缓存 key 前缀 */
    public static final String CACHE_OWNER_BUILDING_KEY = "spmp:owner:building:";

    /** 业主认证状态缓存 key 前缀 */
    public static final String CACHE_OWNER_CERTIFIED_KEY = "spmp:owner:certified:";

    // ========== 缓存过期时间 ==========

    /** 缓存过期时间（小时） */
    public static final long CACHE_EXPIRE_HOURS = 24;

    // ========== 业务限制 ==========

    /** 房产绑定人数上限（含业主本人） */
    public static final int FAMILY_MEMBER_LIMIT = 10;

    /** 批量审批最大条数 */
    public static final int BATCH_APPROVE_LIMIT = 100;

    /** 认证超时天数 */
    public static final int CERTIFICATION_TIMEOUT_DAYS = 7;
}
