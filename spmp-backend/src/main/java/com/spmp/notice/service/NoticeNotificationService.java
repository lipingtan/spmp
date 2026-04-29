package com.spmp.notice.service;

/**
 * 公告通知编排服务。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface NoticeNotificationService {

    /**
     * 按快照批量派发公告通知（首次发布）。
     *
     * @param announcementId 公告ID
     * @return 派发用户数
     */
    int dispatchToSnapshot(Long announcementId);

    /**
     * 重推：对未读用户再次派发。
     *
     * @param announcementId 公告ID
     * @return 派发用户数
     */
    int repushUnread(Long announcementId);
}
