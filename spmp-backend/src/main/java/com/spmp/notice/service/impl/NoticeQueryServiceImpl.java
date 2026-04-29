package com.spmp.notice.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spmp.common.exception.BusinessException;
import com.spmp.common.result.PageResult;
import com.spmp.notice.constant.NoticeErrorCode;
import com.spmp.notice.domain.dto.NoticeMineQueryDTO;
import com.spmp.notice.domain.dto.NoticeQueryDTO;
import com.spmp.notice.domain.entity.AnnouncementDO;
import com.spmp.notice.repository.AnnouncementMapper;
import com.spmp.notice.repository.AnnouncementReadRecordMapper;
import com.spmp.notice.repository.AnnouncementTargetSnapshotMapper;
import com.spmp.notice.domain.vo.NoticeDetailVO;
import com.spmp.notice.domain.vo.NoticeListVO;
import com.spmp.notice.domain.vo.NoticeReadStatsVO;
import com.spmp.notice.domain.vo.NoticeUnreadUserVO;
import com.spmp.notice.service.NoticeQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * 公告查询服务实现。
 */
@Service
@RequiredArgsConstructor
public class NoticeQueryServiceImpl implements NoticeQueryService {

    private final AnnouncementMapper announcementMapper;
    private final AnnouncementReadRecordMapper announcementReadRecordMapper;
    private final AnnouncementTargetSnapshotMapper announcementTargetSnapshotMapper;

    @Override
    public PageResult<NoticeListVO> listManage(NoticeQueryDTO queryDTO) {
        IPage<NoticeListVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        return PageResult.of(announcementMapper.selectManagePage(page, queryDTO));
    }

    @Override
    public PageResult<NoticeListVO> listMine(NoticeMineQueryDTO queryDTO, Long userId) {
        IPage<NoticeListVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<NoticeListVO> resultPage = announcementMapper.selectMinePage(page, userId, queryDTO.getNoticeType());
        return PageResult.of(resultPage);
    }

    @Override
    public NoticeDetailVO getDetailAndMarkRead(Long noticeId, Long userId) {
        AnnouncementDO announcement = announcementMapper.selectById(noticeId);
        if (announcement == null) {
            throw new BusinessException(NoticeErrorCode.NOTICE_NOT_FOUND.getCode(),
                    NoticeErrorCode.NOTICE_NOT_FOUND.getMessage());
        }
        if (!canAccessAnnouncement(announcement, userId)) {
            throw new BusinessException(NoticeErrorCode.NOTICE_PERMISSION_DENIED.getCode(),
                    NoticeErrorCode.NOTICE_PERMISSION_DENIED.getMessage());
        }
        announcementReadRecordMapper.insertIgnore(noticeId, userId);

        NoticeDetailVO detailVO = new NoticeDetailVO();
        detailVO.setId(announcement.getId());
        detailVO.setTitle(announcement.getTitle());
        detailVO.setContent(announcement.getContent());
        detailVO.setNoticeType(announcement.getNoticeType());
        detailVO.setStatus(announcement.getStatus());
        detailVO.setTopFlag(announcement.getTopFlag());
        detailVO.setPublishTime(toLocalDateTime(announcement.getPublishTime()));
        detailVO.setExpireTime(toLocalDateTime(announcement.getExpireTime()));
        detailVO.setExpired(isExpired(announcement.getExpireTime()));
        detailVO.setRead(Boolean.TRUE);
        return detailVO;
    }

    @Override
    public NoticeReadStatsVO getReadStats(Long noticeId) {
        AnnouncementDO announcement = announcementMapper.selectById(noticeId);
        if (announcement == null) {
            throw new BusinessException(NoticeErrorCode.NOTICE_NOT_FOUND.getCode(),
                    NoticeErrorCode.NOTICE_NOT_FOUND.getMessage());
        }
        long targetCount = announcementTargetSnapshotMapper.countTargetByAnnouncement(noticeId);
        long readCount = announcementReadRecordMapper.countReadByAnnouncement(noticeId);
        long unreadCount = Math.max(0, targetCount - readCount);

        NoticeReadStatsVO statsVO = new NoticeReadStatsVO();
        statsVO.setAnnouncementId(noticeId);
        statsVO.setTargetCount(targetCount);
        statsVO.setReadCount(readCount);
        statsVO.setUnreadCount(unreadCount);
        statsVO.setReadRate(targetCount == 0 ? 0D : (double) readCount / (double) targetCount);
        return statsVO;
    }

    @Override
    public PageResult<NoticeUnreadUserVO> listUnreadUsers(Long noticeId, Integer pageNum, Integer pageSize) {
        IPage<NoticeUnreadUserVO> page = new Page<>(pageNum, pageSize);
        return PageResult.of(announcementTargetSnapshotMapper.selectUnreadUsersPage(page, noticeId));
    }

    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private boolean isExpired(Date expireTime) {
        return expireTime != null && expireTime.before(new Date());
    }

    private boolean canAccessAnnouncement(AnnouncementDO announcement, Long userId) {
        if (announcement == null || userId == null) {
            return false;
        }
        boolean publishedAndNotExpired = "PUBLISHED".equals(announcement.getStatus()) && !isExpired(announcement.getExpireTime());
        if (!publishedAndNotExpired) {
            return false;
        }
        return announcementTargetSnapshotMapper.countTargetByAnnouncementAndUser(announcement.getId(), userId) > 0;
    }
}
