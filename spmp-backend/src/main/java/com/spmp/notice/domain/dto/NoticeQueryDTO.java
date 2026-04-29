package com.spmp.notice.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告分页查询 DTO（管理端）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class NoticeQueryDTO {

    private String title;

    private String noticeType;

    private String status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
