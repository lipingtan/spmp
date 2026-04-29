package com.spmp.notice.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 公告已读记录实体。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@TableName("nt_announcement_read_record")
public class AnnouncementReadRecordDO {

    private Long id;

    private Long announcementId;

    private Long userId;

    private Date readTime;
}
