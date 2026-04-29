package com.spmp.workorder.constant;

/**
 * 工单管理模块常量定义。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public final class WorkOrderConstants {

    private WorkOrderConstants() {
    }

    public static final String CACHE_LIST_KEY_PREFIX = "spmp:workorder:list:";
    public static final String CACHE_DETAIL_KEY_PREFIX = "spmp:workorder:detail:";
    public static final String SEQ_KEY_PREFIX = "workorder:seq:";
    public static final String ORDER_NO_PREFIX = "WO";

    public static final int CACHE_TTL_MINUTES = 5;
    public static final int EXPORT_MAX_ROWS = 1000;
    public static final int MAX_IMAGES_PER_TYPE = 5;
    public static final int MAX_REJECT_COUNT = 3;
    public static final int AUTO_VERIFY_DAYS = 7;
    public static final int ACCEPT_REMIND_HOURS = 2;
    public static final int ACCEPT_RETURN_HOURS = 3;
    public static final long MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024;

    public static final String DICT_CATEGORY_WORKORDER_TYPE = "WORKORDER_TYPE";
    public static final String ROLE_REPAIR_STAFF = "REPAIR_STAFF";
}
