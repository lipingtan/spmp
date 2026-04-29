package com.spmp.notice.constant;

/**
 * 公告模块常量定义。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public final class NoticeConstants {

    private NoticeConstants() {
    }

    /** Redis 幂等 Key 前缀。 */
    public static final String REPUSH_IDEMPOTENT_KEY_PREFIX = "spmp:notice:repush:";

    /** 未读列表默认分页大小。 */
    public static final int DEFAULT_UNREAD_PAGE_SIZE = 20;

    /** 未读列表最大分页大小。 */
    public static final int MAX_UNREAD_PAGE_SIZE = 200;

    /** 公告内容最大长度。 */
    public static final int MAX_CONTENT_LENGTH = 20000;
}
