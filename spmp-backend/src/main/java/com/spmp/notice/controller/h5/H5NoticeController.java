package com.spmp.notice.controller.h5;

import com.spmp.common.result.PageResult;
import com.spmp.common.result.Result;
import com.spmp.notice.domain.dto.NoticeMineQueryDTO;
import com.spmp.notice.domain.vo.NoticeDetailVO;
import com.spmp.notice.domain.vo.NoticeListVO;
import com.spmp.notice.service.NoticeQueryService;
import com.spmp.notice.support.NoticeSecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公告 H5 业主端 Controller。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/h5/notice/announcements")
@RequiredArgsConstructor
public class H5NoticeController {

    private final NoticeQueryService noticeQueryService;

    @GetMapping
    public Result<PageResult<NoticeListVO>> listMine(NoticeMineQueryDTO queryDTO) {
        Long userId = NoticeSecurityUtils.getCurrentUserId();
        return Result.success(noticeQueryService.listMine(queryDTO, userId));
    }

    @GetMapping("/{id}")
    public Result<NoticeDetailVO> getDetail(@PathVariable Long id) {
        Long userId = NoticeSecurityUtils.getCurrentUserId();
        return Result.success(noticeQueryService.getDetailAndMarkRead(id, userId));
    }
}
