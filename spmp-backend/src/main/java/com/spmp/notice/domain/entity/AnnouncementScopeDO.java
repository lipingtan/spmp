package com.spmp.notice.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 公告推送范围实体。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@TableName("nt_announcement_scope")
public class AnnouncementScopeDO {

    private Long id;

    private Long announcementId;

    private String scopeType;

    private Long targetId;

    private Date createTime;
}
