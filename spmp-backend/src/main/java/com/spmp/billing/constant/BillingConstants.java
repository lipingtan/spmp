package com.spmp.billing.constant;

/**
 * 缴费管理模块常量定义。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public final class BillingConstants {

    private BillingConstants() {
    }

    public static final String MODULE_NAME = "缴费管理";
    public static final String BILL_NO_PREFIX = "BL";
    public static final String BATCH_NO_PREFIX = "BATCH";
    public static final String BILL_NO_SEQ_PREFIX = "billing:bill:seq:";
    public static final String BATCH_NO_SEQ_PREFIX = "billing:batch:seq:";
    public static final String GENERATE_PROGRESS_PREFIX = "billing:generate:";
    public static final String CACHE_LIST_PREFIX = "billing:list:";
    public static final String CACHE_DETAIL_PREFIX = "billing:detail:";
    public static final String CACHE_STATISTICS_PREFIX = "billing:statistics:";

    public static final int GENERATE_LIMIT = 5000;
    public static final int EXPORT_LIMIT = 10000;
    public static final int CACHE_LIST_TTL_MINUTES = 5;
    public static final int CACHE_STATISTICS_TTL_MINUTES = 10;
    public static final int PAYMENT_TIMEOUT_MINUTES = 30;
    public static final int URGE_REMIND_DAYS = 7;

    public static final String CONFIG_STATUS_ENABLED = "ENABLED";
    public static final String CONFIG_STATUS_DISABLED = "DISABLED";
}
