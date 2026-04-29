package com.spmp.notice.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 公告审批实例实体。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@TableName("nt_announcement_approval")
public class AnnouncementApprovalDO {

    private Long id;

    private Long announcementId;

    private String approvalStatus;

    private Long currentNodeId;

    private Date submitTime;

    private Date completeTime;
}
