package com.spmp.billing.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 减免记录 VO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class ReduceRecordVO {
    private Long id;
    private String billNo;
    private BigDecimal reduceAmount;
    private String reason;
    private String status;
    private String statusName;
    private String applicantName;
    private String approverName;
    private LocalDateTime approveTime;
    private String approveRemark;
    private Date createTime;
}
