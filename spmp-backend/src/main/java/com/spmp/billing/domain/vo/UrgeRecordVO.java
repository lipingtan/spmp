package com.spmp.billing.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 催收记录 VO。
 */
@Data
public class UrgeRecordVO {
    private Long id;
    private String billNo;
    private String urgeType;
    private String urgeTypeName;
    private String urgeUserName;
    private LocalDateTime urgeTime;
    private String feedback;
}
