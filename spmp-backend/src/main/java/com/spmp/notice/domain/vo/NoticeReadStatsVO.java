package com.spmp.notice.domain.vo;

import lombok.Data;

/**
 * 公告已读统计 VO。
 */
@Data
public class NoticeReadStatsVO {

    private Long announcementId;

    private Long targetCount;

    private Long readCount;

    private Long unreadCount;

    private Double readRate;
}
