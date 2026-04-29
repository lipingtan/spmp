package com.spmp.notice.service;

import com.spmp.notice.domain.dto.NoticeApproveDTO;
import com.spmp.notice.domain.dto.NoticeCreateDTO;
import com.spmp.notice.domain.dto.NoticeRepushDTO;

/**
 * 公告发布流转服务。
 */
public interface NoticePublishService {

    Long createAndSubmit(NoticeCreateDTO createDTO, Long operatorId, String operatorName);

    void approve(Long noticeId, NoticeApproveDTO approveDTO, Long operatorId, String operatorName);

    void withdraw(Long noticeId, Long operatorId, String operatorName, String remark);

    void repush(Long noticeId, NoticeRepushDTO repushDTO, Long operatorId, String operatorName);
}
