package com.spmp.notice.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.spmp.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 公告主表实体。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("nt_announcement")
public class AnnouncementDO extends BaseEntity {

    private String title;

    private String content;

    private String noticeType;

    private String status;

    private String approvalStatus;

    private Integer topFlag;

    private Date publishTime;

    private Date expireTime;

    private Long creatorId;
}
