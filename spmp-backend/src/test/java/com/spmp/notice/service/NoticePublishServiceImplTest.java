package com.spmp.notice.service;

import com.spmp.common.exception.BusinessException;
import com.spmp.notice.constant.NoticeStatus;
import com.spmp.notice.domain.dto.NoticeRepushDTO;
import com.spmp.notice.domain.entity.AnnouncementDO;
import com.spmp.notice.domain.entity.AnnouncementScopeDO;
import com.spmp.notice.repository.AnnouncementApprovalMapper;
import com.spmp.notice.repository.AnnouncementMapper;
import com.spmp.notice.repository.AnnouncementOperationLogMapper;
import com.spmp.notice.repository.AnnouncementScopeMapper;
import com.spmp.notice.repository.AnnouncementTargetSnapshotMapper;
import com.spmp.notice.service.impl.NoticePublishServiceImpl;
import com.spmp.notice.support.NoticeRichTextSanitizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("公告发布服务测试")
class NoticePublishServiceImplTest {

    @Mock
    private AnnouncementMapper announcementMapper;
    @Mock
    private AnnouncementScopeMapper announcementScopeMapper;
    @Mock
    private AnnouncementApprovalMapper announcementApprovalMapper;
    @Mock
    private AnnouncementOperationLogMapper announcementOperationLogMapper;
    @Mock
    private AnnouncementTargetSnapshotMapper announcementTargetSnapshotMapper;
    @Mock
    private NoticeNotificationService noticeNotificationService;
    @Mock
    private NoticeRichTextSanitizer richTextSanitizer;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private NoticePublishServiceImpl noticePublishService;

    @Test
    @DisplayName("重推前补齐快照并执行重推")
    void shouldGenerateSnapshotBeforeRepushUnread() {
        Long noticeId = 1001L;
        AnnouncementDO notice = new AnnouncementDO();
        notice.setId(noticeId);
        notice.setStatus(NoticeStatus.PUBLISHED.name());

        AnnouncementScopeDO allScope = new AnnouncementScopeDO();
        allScope.setScopeType("ALL");
        AnnouncementScopeDO communityScope = new AnnouncementScopeDO();
        communityScope.setScopeType("COMMUNITY");
        communityScope.setTargetId(10L);

        NoticeRepushDTO repushDTO = new NoticeRepushDTO();
        repushDTO.setBizSerialNo("biz-1001");
        repushDTO.setRemark("重推测试");

        when(announcementMapper.selectById(noticeId)).thenReturn(notice);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(any(), eq(noticeId), anyLong(), any())).thenReturn(Boolean.TRUE);
        when(announcementScopeMapper.selectList(any())).thenReturn(Arrays.asList(allScope, communityScope));
        when(announcementTargetSnapshotMapper.insertSnapshotByScope(noticeId, "ALL", null)).thenReturn(3);
        when(announcementTargetSnapshotMapper.insertSnapshotByScope(noticeId, "COMMUNITY", 10L)).thenReturn(2);

        assertDoesNotThrow(() -> noticePublishService.repush(noticeId, repushDTO, 1L, "tester"));

        verify(announcementTargetSnapshotMapper).insertSnapshotByScope(noticeId, "ALL", null);
        verify(announcementTargetSnapshotMapper).insertSnapshotByScope(noticeId, "COMMUNITY", 10L);
        verify(noticeNotificationService).repushUnread(noticeId);
    }

    @Test
    @DisplayName("重复重推命中幂等键应拒绝执行")
    void shouldRejectRepushWhenIdempotentKeyHit() {
        Long noticeId = 1002L;
        AnnouncementDO notice = new AnnouncementDO();
        notice.setId(noticeId);
        notice.setStatus(NoticeStatus.PUBLISHED.name());

        NoticeRepushDTO repushDTO = new NoticeRepushDTO();
        repushDTO.setBizSerialNo("biz-1002");

        when(announcementMapper.selectById(noticeId)).thenReturn(notice);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(any(), eq(noticeId), anyLong(), any())).thenReturn(Boolean.FALSE);

        assertThrows(BusinessException.class, () -> noticePublishService.repush(noticeId, repushDTO, 1L, "tester"));
        verify(announcementTargetSnapshotMapper, never()).insertSnapshotByScope(anyLong(), any(), any());
        verify(noticeNotificationService, never()).repushUnread(anyLong());
    }
}
