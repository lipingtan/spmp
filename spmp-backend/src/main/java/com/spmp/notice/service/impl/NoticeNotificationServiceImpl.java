package com.spmp.notice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spmp.notice.domain.entity.AnnouncementDO;
import com.spmp.notice.domain.entity.AnnouncementTargetSnapshotDO;
import com.spmp.notice.domain.vo.NoticeUnreadUserVO;
import com.spmp.notice.repository.AnnouncementMapper;
import com.spmp.notice.repository.AnnouncementTargetSnapshotMapper;
import com.spmp.notice.service.NoticeChannelSender;
import com.spmp.notice.service.NoticeNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 公告通知编排服务实现。
 * <p>
 * 基于 {@link NoticeChannelSender} SPI，逐用户遍历所有启用通道执行派发。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeNotificationServiceImpl implements NoticeNotificationService {

    private static final int PAGE_SIZE = 500;

    private final AnnouncementMapper announcementMapper;
    private final AnnouncementTargetSnapshotMapper announcementTargetSnapshotMapper;
    private final List<NoticeChannelSender> channelSenders;

    @Override
    public int dispatchToSnapshot(Long announcementId) {
        AnnouncementDO announcement = announcementMapper.selectById(announcementId);
        if (announcement == null) {
            return 0;
        }

        int total = 0;
        long current = 1;
        while (true) {
            IPage<AnnouncementTargetSnapshotDO> page = new Page<>(current, PAGE_SIZE);
            IPage<AnnouncementTargetSnapshotDO> result = announcementTargetSnapshotMapper.selectPage(page,
                    new LambdaQueryWrapper<AnnouncementTargetSnapshotDO>()
                            .eq(AnnouncementTargetSnapshotDO::getAnnouncementId, announcementId)
                            .orderByAsc(AnnouncementTargetSnapshotDO::getId));
            if (result.getRecords() == null || result.getRecords().isEmpty()) {
                break;
            }
            for (AnnouncementTargetSnapshotDO snapshot : result.getRecords()) {
                sendAll(announcement, snapshot.getUserId());
                total++;
            }
            if (result.getRecords().size() < PAGE_SIZE) {
                break;
            }
            current++;
        }
        log.info("[Notice] dispatchToSnapshot announcementId={} totalUsers={}", announcementId, total);
        return total;
    }

    @Override
    public int repushUnread(Long announcementId) {
        AnnouncementDO announcement = announcementMapper.selectById(announcementId);
        if (announcement == null) {
            return 0;
        }
        int total = 0;
        long current = 1;
        while (true) {
            IPage<NoticeUnreadUserVO> page = new Page<>(current, PAGE_SIZE);
            IPage<NoticeUnreadUserVO> result = announcementTargetSnapshotMapper.selectUnreadUsersPage(page, announcementId);
            if (result.getRecords() == null || result.getRecords().isEmpty()) {
                break;
            }
            for (NoticeUnreadUserVO unread : result.getRecords()) {
                sendAll(announcement, unread.getUserId());
                total++;
            }
            if (result.getRecords().size() < PAGE_SIZE) {
                break;
            }
            current++;
        }
        log.info("[Notice] repushUnread announcementId={} unreadUsers={}", announcementId, total);
        return total;
    }

    private void sendAll(AnnouncementDO announcement, Long userId) {
        for (NoticeChannelSender sender : channelSenders) {
            try {
                if (sender.supports(announcement)) {
                    sender.send(announcement, userId);
                }
            } catch (Exception e) {
                log.warn("[Notice] channel {} send failed, announcementId={} userId={}",
                        sender.channelName(), announcement.getId(), userId, e);
            }
        }
    }
}
