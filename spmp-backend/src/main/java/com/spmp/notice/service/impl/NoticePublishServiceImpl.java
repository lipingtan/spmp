package com.spmp.notice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.spmp.common.exception.BusinessException;
import com.spmp.notice.constant.ApprovalStatus;
import com.spmp.notice.constant.NoticeConstants;
import com.spmp.notice.constant.NoticeErrorCode;
import com.spmp.notice.constant.NoticeOperationType;
import com.spmp.notice.constant.NoticeStatus;
import com.spmp.notice.domain.dto.NoticeApproveDTO;
import com.spmp.notice.domain.dto.NoticeCreateDTO;
import com.spmp.notice.domain.dto.NoticeRepushDTO;
import com.spmp.notice.domain.dto.NoticeScopeDTO;
import com.spmp.notice.domain.entity.AnnouncementApprovalDO;
import com.spmp.notice.domain.entity.AnnouncementDO;
import com.spmp.notice.domain.entity.AnnouncementOperationLogDO;
import com.spmp.notice.domain.entity.AnnouncementScopeDO;
import com.spmp.notice.repository.AnnouncementApprovalMapper;
import com.spmp.notice.repository.AnnouncementMapper;
import com.spmp.notice.repository.AnnouncementOperationLogMapper;
import com.spmp.notice.repository.AnnouncementScopeMapper;
import com.spmp.notice.repository.AnnouncementTargetSnapshotMapper;
import com.spmp.notice.service.NoticeNotificationService;
import com.spmp.notice.service.NoticePublishService;
import com.spmp.notice.support.NoticeRichTextSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 公告发布流转服务实现。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoticePublishServiceImpl implements NoticePublishService {

    private final AnnouncementMapper announcementMapper;
    private final AnnouncementScopeMapper announcementScopeMapper;
    private final AnnouncementApprovalMapper announcementApprovalMapper;
    private final AnnouncementOperationLogMapper announcementOperationLogMapper;
    private final AnnouncementTargetSnapshotMapper announcementTargetSnapshotMapper;
    private final NoticeNotificationService noticeNotificationService;
    private final NoticeRichTextSanitizer richTextSanitizer;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAndSubmit(NoticeCreateDTO createDTO, Long operatorId, String operatorName) {
        AnnouncementDO announcement = new AnnouncementDO();
        announcement.setTitle(createDTO.getTitle());
        announcement.setContent(richTextSanitizer.sanitize(createDTO.getContent()));
        announcement.setNoticeType(createDTO.getNoticeType());
        announcement.setTopFlag(createDTO.getTopFlag() == null ? 0 : createDTO.getTopFlag());
        announcement.setStatus(NoticeStatus.PENDING_APPROVAL.name());
        announcement.setApprovalStatus(ApprovalStatus.PENDING.name());
        announcement.setCreatorId(operatorId);
        announcement.setExpireTime(toDate(createDTO.getExpireTime()));
        announcementMapper.insert(announcement);

        for (NoticeScopeDTO scopeDTO : createDTO.getScopeList()) {
            AnnouncementScopeDO scope = new AnnouncementScopeDO();
            scope.setAnnouncementId(announcement.getId());
            scope.setScopeType(scopeDTO.getScopeType());
            scope.setTargetId(scopeDTO.getTargetId());
            announcementScopeMapper.insert(scope);
        }

        AnnouncementApprovalDO approval = new AnnouncementApprovalDO();
        approval.setAnnouncementId(announcement.getId());
        approval.setApprovalStatus(ApprovalStatus.PENDING.name());
        approval.setSubmitTime(new Date());
        announcementApprovalMapper.insert(approval);

        saveOperationLog(announcement.getId(), NoticeOperationType.CREATE.name(), operatorId, operatorName, "提交审批");
        return announcement.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long noticeId, NoticeApproveDTO approveDTO, Long operatorId, String operatorName) {
        AnnouncementDO announcement = getNoticeOrThrow(noticeId);
        if (!NoticeStatus.PENDING_APPROVAL.name().equals(announcement.getStatus())) {
            throw new BusinessException(NoticeErrorCode.NOTICE_STATUS_INVALID.getCode(), "当前状态不允许审批");
        }
        AnnouncementApprovalDO approval = announcementApprovalMapper.selectOne(
                new LambdaQueryWrapper<AnnouncementApprovalDO>()
                        .eq(AnnouncementApprovalDO::getAnnouncementId, noticeId)
                        .last("LIMIT 1")
        );
        if (approval == null) {
            throw new BusinessException(NoticeErrorCode.NOTICE_APPROVAL_NOT_FOUND.getCode(),
                    NoticeErrorCode.NOTICE_APPROVAL_NOT_FOUND.getMessage());
        }

        boolean approved = "APPROVE".equalsIgnoreCase(approveDTO.getAction());
        if (approved) {
            announcement.setStatus(NoticeStatus.PUBLISHED.name());
            announcement.setApprovalStatus(ApprovalStatus.APPROVED.name());
            if (announcement.getPublishTime() == null) {
                announcement.setPublishTime(new Date());
            }
            approval.setApprovalStatus(ApprovalStatus.APPROVED.name());
            approval.setCompleteTime(new Date());
            saveOperationLog(noticeId, NoticeOperationType.APPROVE.name(), operatorId, operatorName, approveDTO.getRemark());
        } else {
            announcement.setApprovalStatus(ApprovalStatus.REJECTED.name());
            approval.setApprovalStatus(ApprovalStatus.REJECTED.name());
            approval.setCompleteTime(new Date());
            saveOperationLog(noticeId, NoticeOperationType.REJECT.name(), operatorId, operatorName, approveDTO.getRemark());
        }
        announcementMapper.updateById(announcement);
        announcementApprovalMapper.updateById(approval);

        if (approved) {
            generateTargetSnapshots(noticeId);
            registerAfterCommit(() -> noticeNotificationService.dispatchToSnapshot(noticeId));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdraw(Long noticeId, Long operatorId, String operatorName, String remark) {
        AnnouncementDO announcement = getNoticeOrThrow(noticeId);
        if (!NoticeStatus.PUBLISHED.name().equals(announcement.getStatus())) {
            throw new BusinessException(NoticeErrorCode.NOTICE_STATUS_INVALID.getCode(), "仅已发布公告可撤回");
        }
        announcement.setStatus(NoticeStatus.WITHDRAWN.name());
        announcementMapper.updateById(announcement);
        saveOperationLog(noticeId, NoticeOperationType.WITHDRAW.name(), operatorId, operatorName, remark);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void repush(Long noticeId, NoticeRepushDTO repushDTO, Long operatorId, String operatorName) {
        AnnouncementDO announcement = getNoticeOrThrow(noticeId);
        if (!NoticeStatus.PUBLISHED.name().equals(announcement.getStatus())) {
            throw new BusinessException(NoticeErrorCode.NOTICE_STATUS_INVALID.getCode(), "仅已发布公告可重推");
        }
        String key = NoticeConstants.REPUSH_IDEMPOTENT_KEY_PREFIX + repushDTO.getBizSerialNo();
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(key, noticeId, 10, TimeUnit.MINUTES);
        if (Boolean.FALSE.equals(locked)) {
            throw new BusinessException(NoticeErrorCode.NOTICE_REPUSH_IDEMPOTENT_HIT.getCode(),
                    NoticeErrorCode.NOTICE_REPUSH_IDEMPOTENT_HIT.getMessage());
        }
        saveOperationLog(noticeId, NoticeOperationType.REPUSH.name(), operatorId, operatorName, repushDTO.getRemark());
        // 方案A：重推前先增量补齐快照，确保新登录用户可见
        generateTargetSnapshots(noticeId);
        registerAfterCommit(() -> noticeNotificationService.repushUnread(noticeId));
    }

    private void generateTargetSnapshots(Long noticeId) {
        List<AnnouncementScopeDO> scopes = announcementScopeMapper.selectList(
                new LambdaQueryWrapper<AnnouncementScopeDO>()
                        .eq(AnnouncementScopeDO::getAnnouncementId, noticeId));
        if (scopes == null || scopes.isEmpty()) {
            log.warn("[Notice] no scope found for announcementId={}", noticeId);
            return;
        }
        int total = 0;
        for (AnnouncementScopeDO scope : scopes) {
            int affected = announcementTargetSnapshotMapper.insertSnapshotByScope(
                    noticeId, scope.getScopeType(), scope.getTargetId());
            total += affected;
            log.info("[Notice] snapshot scope processed announcementId={} scopeType={} targetId={} insertedRows={}",
                    noticeId, scope.getScopeType(), scope.getTargetId(), affected);
        }
        log.info("[Notice] snapshot generated announcementId={} insertedRows={}", noticeId, total);
    }

    private void registerAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        action.run();
                    } catch (Exception e) {
                        log.warn("[Notice] after-commit action failed", e);
                    }
                }
            });
        } else {
            action.run();
        }
    }

    private AnnouncementDO getNoticeOrThrow(Long noticeId) {
        AnnouncementDO announcement = announcementMapper.selectById(noticeId);
        if (announcement == null) {
            throw new BusinessException(NoticeErrorCode.NOTICE_NOT_FOUND.getCode(),
                    NoticeErrorCode.NOTICE_NOT_FOUND.getMessage());
        }
        return announcement;
    }

    private void saveOperationLog(Long noticeId, String operationType, Long operatorId, String operatorName, String remark) {
        AnnouncementOperationLogDO logDO = new AnnouncementOperationLogDO();
        logDO.setAnnouncementId(noticeId);
        logDO.setOperationType(operationType);
        logDO.setOperatorId(operatorId);
        logDO.setOperatorName(operatorName);
        logDO.setRemark(remark);
        logDO.setOperationTime(new Date());
        announcementOperationLogMapper.insert(logDO);
    }

    private Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
