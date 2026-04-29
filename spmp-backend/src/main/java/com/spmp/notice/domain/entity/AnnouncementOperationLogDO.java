package com.spmp.notice.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 公告业务操作日志实体。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@TableName("nt_announcement_operation_log")
public class AnnouncementOperationLogDO {

    private Long id;

    private Long announcementId;

    private String operationType;

    private Long operatorId;

    private String operatorName;

    private String remark;

    private Date operationTime;
}
