package com.spmp.notice.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告列表 VO。
 */
@Data
public class NoticeListVO {

    private Long id;

    private String title;

    private String noticeType;

    private String status;

    private Integer topFlag;

    private LocalDateTime publishTime;

    private LocalDateTime expireTime;

    private Boolean expired;

    private Boolean read;
}
