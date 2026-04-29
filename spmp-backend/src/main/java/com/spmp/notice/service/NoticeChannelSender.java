package com.spmp.notice.service;

import com.spmp.notice.domain.entity.AnnouncementDO;

/**
 * 公告通知通道发送器（SPI）。
 * <p>
 * 不同通道（站内、短信、微信、邮件等）实现该接口，由 NoticeNotificationService 统一调度。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface NoticeChannelSender {

    /**
     * 通道名称（唯一标识，便于日志与路由）。
     */
    String channelName();

    /**
     * 是否对当前公告启用该通道。
     */
    boolean supports(AnnouncementDO announcement);

    /**
     * 执行通知发送。
     *
     * @param announcement 公告对象
     * @param userId 目标用户ID
     */
    void send(AnnouncementDO announcement, Long userId);
}
