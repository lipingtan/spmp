package com.spmp.base.constant;

/**
 * 基础数据模块常量定义。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public final class BaseConstants {

    private BaseConstants() {
    }

    // ========== Redis Key 前缀 ==========

    /** 片区缓存 key 前缀 */
    public static final String CACHE_DISTRICT_KEY = "spmp:base:district:";

    /** 小区缓存 key 前缀 */
    public static final String CACHE_COMMUNITY_KEY = "spmp:base:community:";

    /** 楼栋缓存 key 前缀 */
    public static final String CACHE_BUILDING_KEY = "spmp:base:building:";

    /** 单元缓存 key 前缀 */
    public static final String CACHE_UNIT_KEY = "spmp:base:unit:";

    /** 房屋缓存 key 前缀 */
    public static final String CACHE_HOUSE_KEY = "spmp:base:house:";

    /** 级联查询缓存 key 前缀 */
    public static final String CACHE_CASCADE_KEY = "spmp:base:cascade:";

    /** 完整树缓存 key 前缀 */
    public static final String CACHE_TREE_KEY = "spmp:base:tree:";

    /** 所有启用片区ID列表缓存 key */
    public static final String CACHE_DISTRICT_ALL_IDS_KEY = "spmp:base:district:all-ids";

    // ========== 缓存过期时间 ==========

    /** 缓存过期时间（小时） */
    public static final long CACHE_EXPIRE_HOURS = 24;

    // ========== Excel 导入 ==========

    /** 单次导入最大行数 */
    public static final int EXCEL_MAX_IMPORT_ROWS = 5000;

    /** 批量保存大小 */
    public static final int EXCEL_BATCH_SIZE = 500;

    // ========== 编码前缀 ==========

    /** 片区编码前缀 */
    public static final String DISTRICT_CODE_PREFIX = "DIS";

    /** 小区编码前缀 */
    public static final String COMMUNITY_CODE_PREFIX = "COM";

    /** 编码序号长度 */
    public static final int CODE_SEQ_LENGTH = 6;

    // ========== 状态 ==========

    /** 启用 */
    public static final int STATUS_ENABLED = 0;

    /** 停用 */
    public static final int STATUS_DISABLED = 1;
}
