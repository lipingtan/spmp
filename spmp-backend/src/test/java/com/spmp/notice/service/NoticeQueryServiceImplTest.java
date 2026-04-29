package com.spmp.notice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spmp.common.exception.BusinessException;
import com.spmp.common.result.PageResult;
import com.spmp.notice.domain.dto.NoticeMineQueryDTO;
import com.spmp.notice.domain.entity.AnnouncementDO;
import com.spmp.notice.domain.vo.NoticeDetailVO;
import com.spmp.notice.domain.vo.NoticeListVO;
import com.spmp.notice.domain.vo.NoticeReadStatsVO;
import com.spmp.notice.repository.AnnouncementMapper;
import com.spmp.notice.repository.AnnouncementReadRecordMapper;
import com.spmp.notice.repository.AnnouncementTargetSnapshotMapper;
import com.spmp.notice.service.impl.NoticeQueryServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("公告查询服务测试")
class NoticeQueryServiceImplTest {

    @Mock
    private AnnouncementMapper announcementMapper;
    @Mock
    private AnnouncementReadRecordMapper announcementReadRecordMapper;
    @Mock
    private AnnouncementTargetSnapshotMapper announcementTargetSnapshotMapper;

    @InjectMocks
    private NoticeQueryServiceImpl noticeQueryService;

    @Test
    @DisplayName("我的公告列表返回分页结果")
    void shouldListMinePage() {
        NoticeMineQueryDTO queryDTO = new NoticeMineQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(10);
        queryDTO.setNoticeType("GENERAL");

        IPage<NoticeListVO> page = new Page<>(1, 10);
        page.setRecords(Collections.singletonList(new NoticeListVO()));
        page.setTotal(1L);
        when(announcementMapper.selectMinePage(any(Page.class), eq(2001L), eq("GENERAL"))).thenReturn(page);

        PageResult<NoticeListVO> result = noticeQueryService.listMine(queryDTO, 2001L);

        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getData().size());
    }

    @Test
    @DisplayName("详情查询未命中快照时拒绝访问")
    void shouldDenyDetailWhenUserNotInSnapshot() {
        AnnouncementDO announcement = new AnnouncementDO();
        announcement.setId(3001L);
        announcement.setStatus("PUBLISHED");
        announcement.setExpireTime(new Date(System.currentTimeMillis() + 60_000));

        when(announcementMapper.selectById(3001L)).thenReturn(announcement);
        when(announcementTargetSnapshotMapper.countTargetByAnnouncementAndUser(3001L, 9001L)).thenReturn(0L);

        assertThrows(BusinessException.class, () -> noticeQueryService.getDetailAndMarkRead(3001L, 9001L));
        verify(announcementReadRecordMapper, never()).insertIgnore(3001L, 9001L);
    }

    @Test
    @DisplayName("详情查询命中快照时返回详情并写入已读")
    void shouldReturnDetailAndMarkReadWhenUserInSnapshot() {
        AnnouncementDO announcement = new AnnouncementDO();
        announcement.setId(3002L);
        announcement.setTitle("测试公告");
        announcement.setContent("公告正文");
        announcement.setNoticeType("GENERAL");
        announcement.setStatus("PUBLISHED");
        announcement.setExpireTime(new Date(System.currentTimeMillis() + 60_000));
        announcement.setPublishTime(new Date());

        when(announcementMapper.selectById(3002L)).thenReturn(announcement);
        when(announcementTargetSnapshotMapper.countTargetByAnnouncementAndUser(3002L, 9002L)).thenReturn(1L);
        when(announcementReadRecordMapper.insertIgnore(3002L, 9002L)).thenReturn(1);

        NoticeDetailVO detail = noticeQueryService.getDetailAndMarkRead(3002L, 9002L);

        assertNotNull(detail);
        assertEquals(3002L, detail.getId());
        assertEquals(Boolean.TRUE, detail.getRead());
        verify(announcementReadRecordMapper).insertIgnore(3002L, 9002L);
    }

    @Test
    @DisplayName("阅读统计未读数不应小于0")
    void shouldKeepUnreadCountNonNegative() {
        AnnouncementDO announcement = new AnnouncementDO();
        announcement.setId(4001L);
        when(announcementMapper.selectById(4001L)).thenReturn(announcement);
        when(announcementTargetSnapshotMapper.countTargetByAnnouncement(4001L)).thenReturn(3L);
        when(announcementReadRecordMapper.countReadByAnnouncement(4001L)).thenReturn(5L);

        NoticeReadStatsVO stats = noticeQueryService.getReadStats(4001L);

        assertEquals(0L, stats.getUnreadCount());
        assertEquals(3L, stats.getTargetCount());
        assertEquals(5L, stats.getReadCount());
    }
}
