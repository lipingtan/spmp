package com.spmp.notice.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 公告审批节点实体。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@TableName("nt_announcement_approval_node")
public class AnnouncementApprovalNodeDO {

    private Long id;

    private Long approvalId;

    private Integer nodeOrder;

    private Long approverId;

    private String nodeStatus;

    private Date approveTime;

    private String approveRemark;
}
