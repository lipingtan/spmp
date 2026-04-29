package com.spmp.notice.controller;

import com.spmp.common.result.PageResult;
import com.spmp.common.result.Result;
import com.spmp.notice.domain.dto.NoticeApproveDTO;
import com.spmp.notice.domain.dto.NoticeQueryDTO;
import com.spmp.notice.domain.vo.NoticeListVO;
import com.spmp.notice.service.NoticePublishService;
import com.spmp.notice.service.NoticeQueryService;
import com.spmp.notice.support.NoticeSecurityUtils;
import com.spmp.user.annotation.OperationLog;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 公告审批 Controller。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/notice/approvals")
@RequiredArgsConstructor
public class NoticeApprovalController {

    private final NoticePublishService noticePublishService;
    private final NoticeQueryService noticeQueryService;

    /**
     * 待审批公告列表（默认筛选 PENDING_APPROVAL 状态）。
     */
    @GetMapping
    @PreAuthorize("@perm.check('notice:approve')")
    public PageResult<NoticeListVO> listPending(
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        NoticeQueryDTO queryDTO = new NoticeQueryDTO();
        queryDTO.setTitle(title);
        queryDTO.setStatus("PENDING_APPROVAL");
        queryDTO.setPageNum(pageNum);
        queryDTO.setPageSize(pageSize);
        return noticeQueryService.listManage(queryDTO);
    }

    /**
     * 审批动作：APPROVE / REJECT。
     */
    @PutMapping("/{id}")
    @PreAuthorize("@perm.check('notice:approve')")
    @OperationLog(module = "公告管理", type = "APPROVE", description = "审批公告")
    public Result<Void> approve(@PathVariable Long id,
                                @Valid @RequestBody NoticeApproveDTO approveDTO) {
        Long operatorId = NoticeSecurityUtils.getCurrentUserId();
        String operatorName = NoticeSecurityUtils.getCurrentUsername();
        noticePublishService.approve(id, approveDTO, operatorId, operatorName);
        return Result.success(null);
    }
}
