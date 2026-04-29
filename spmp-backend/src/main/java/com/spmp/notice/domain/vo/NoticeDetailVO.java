package com.spmp.notice.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告详情 VO。
 */
@Data
public class NoticeDetailVO {

    private Long id;

    private String title;

    private String content;

    private String noticeType;

    private String status;

    private Integer topFlag;

    private LocalDateTime publishTime;

    private LocalDateTime expireTime;

    private Boolean expired;

    private Boolean read;
}
