package com.spmp.notice.controller;

import com.spmp.common.result.PageResult;
import com.spmp.common.result.Result;
import com.spmp.notice.constant.NoticeConstants;
import com.spmp.notice.domain.dto.NoticeCreateDTO;
import com.spmp.notice.domain.dto.NoticeQueryDTO;
import com.spmp.notice.domain.dto.NoticeRepushDTO;
import com.spmp.notice.domain.vo.NoticeListVO;
import com.spmp.notice.domain.vo.NoticeReadStatsVO;
import com.spmp.notice.domain.vo.NoticeUnreadUserVO;
import com.spmp.notice.service.NoticePublishService;
import com.spmp.notice.service.NoticeQueryService;
import com.spmp.notice.support.NoticeSecurityUtils;
import com.spmp.user.annotation.OperationLog;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 公告管理 Controller（PC 管理端）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/notice/announcements")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticePublishService noticePublishService;
    private final NoticeQueryService noticeQueryService;

    @GetMapping
    @PreAuthorize("@perm.check('notice:list')")
    public PageResult<NoticeListVO> listNotices(NoticeQueryDTO queryDTO) {
        return noticeQueryService.listManage(queryDTO);
    }

    @PostMapping
    @PreAuthorize("@perm.check('notice:create')")
    @OperationLog(module = "公告管理", type = "CREATE", description = "创建公告并提交审批")
    public Result<Long> createNotice(@Valid @RequestBody NoticeCreateDTO createDTO) {
        Long operatorId = NoticeSecurityUtils.getCurrentUserId();
        String operatorName = NoticeSecurityUtils.getCurrentUsername();
        Long id = noticePublishService.createAndSubmit(createDTO, operatorId, operatorName);
        return Result.success(id);
    }

    @PutMapping("/{id}/withdraw")
    @PreAuthorize("@perm.check('notice:withdraw')")
    @OperationLog(module = "公告管理", type = "WITHDRAW", description = "撤回公告")
    public Result<Void> withdrawNotice(@PathVariable Long id,
                                       @RequestParam(required = false) String remark) {
        Long operatorId = NoticeSecurityUtils.getCurrentUserId();
        String operatorName = NoticeSecurityUtils.getCurrentUsername();
        noticePublishService.withdraw(id, operatorId, operatorName, remark);
        return Result.success(null);
    }

    @PostMapping("/{id}/repush")
    @PreAuthorize("@perm.check('notice:repush')")
    @OperationLog(module = "公告管理", type = "REPUSH", description = "公告重推")
    public Result<Void> repushNotice(@PathVariable Long id,
                                     @Valid @RequestBody NoticeRepushDTO repushDTO) {
        Long operatorId = NoticeSecurityUtils.getCurrentUserId();
        String operatorName = NoticeSecurityUtils.getCurrentUsername();
        noticePublishService.repush(id, repushDTO, operatorId, operatorName);
        return Result.success(null);
    }

    @GetMapping("/{id}/stats")
    @PreAuthorize("@perm.check('notice:stats')")
    public Result<NoticeReadStatsVO> getReadStats(@PathVariable Long id) {
        return Result.success(noticeQueryService.getReadStats(id));
    }

    @GetMapping("/{id}/unread-users")
    @PreAuthorize("@perm.check('notice:stats')")
    public PageResult<NoticeUnreadUserVO> listUnreadUsers(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        int effective = Math.min(pageSize == null ? NoticeConstants.DEFAULT_UNREAD_PAGE_SIZE : pageSize,
                NoticeConstants.MAX_UNREAD_PAGE_SIZE);
        return noticeQueryService.listUnreadUsers(id, pageNum == null ? 1 : pageNum, effective);
    }
}
