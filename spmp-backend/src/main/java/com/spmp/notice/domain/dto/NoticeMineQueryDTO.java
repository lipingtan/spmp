package com.spmp.notice.domain.dto;

import lombok.Data;

/**
 * 业主端公告查询 DTO。
 */
@Data
public class NoticeMineQueryDTO {

    private String noticeType;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
