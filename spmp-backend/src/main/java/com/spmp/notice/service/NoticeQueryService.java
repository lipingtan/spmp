package com.spmp.notice.service;

import com.spmp.common.result.PageResult;
import com.spmp.notice.domain.dto.NoticeMineQueryDTO;
import com.spmp.notice.domain.dto.NoticeQueryDTO;
import com.spmp.notice.domain.vo.NoticeDetailVO;
import com.spmp.notice.domain.vo.NoticeListVO;
import com.spmp.notice.domain.vo.NoticeReadStatsVO;
import com.spmp.notice.domain.vo.NoticeUnreadUserVO;

/**
 * 公告查询服务。
 */
public interface NoticeQueryService {

    PageResult<NoticeListVO> listManage(NoticeQueryDTO queryDTO);

    PageResult<NoticeListVO> listMine(NoticeMineQueryDTO queryDTO, Long userId);

    NoticeDetailVO getDetailAndMarkRead(Long noticeId, Long userId);

    NoticeReadStatsVO getReadStats(Long noticeId);

    PageResult<NoticeUnreadUserVO> listUnreadUsers(Long noticeId, Integer pageNum, Integer pageSize);
}
