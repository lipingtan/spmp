package com.spmp.notice.domain.vo;

import lombok.Data;

/**
 * 公告未读用户 VO。
 */
@Data
public class NoticeUnreadUserVO {

    private Long userId;

    private String userName;

    private Long communityId;

    private Long buildingId;
}
