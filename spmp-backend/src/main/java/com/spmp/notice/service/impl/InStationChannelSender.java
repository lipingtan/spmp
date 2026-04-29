package com.spmp.notice.service.impl;

import com.spmp.notice.domain.entity.AnnouncementDO;
import com.spmp.notice.service.NoticeChannelSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 站内通知通道（默认通道）。
 * <p>
 * MVP 版本仅打印日志；后续接入站内消息表或 WebSocket 推送。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Component
public class InStationChannelSender implements NoticeChannelSender {

    @Override
    public String channelName() {
        return "IN_STATION";
    }

    @Override
    public boolean supports(AnnouncementDO announcement) {
        return true;
    }

    @Override
    public void send(AnnouncementDO announcement, Long userId) {
        log.info("[Notice] in-station push announcementId={} userId={} title={}",
                announcement.getId(), userId, announcement.getTitle());
    }
}
