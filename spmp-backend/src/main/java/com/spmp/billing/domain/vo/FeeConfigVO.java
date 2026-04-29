package com.spmp.billing.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 费用配置 VO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class FeeConfigVO {

    private Long id;
    private Long communityId;
    private String communityName;
    private Long buildingId;
    private String buildingName;
    private String feeType;
    private String feeTypeName;
    private String billingMethod;
    private String billingMethodName;
    private BigDecimal unitPrice;
    private Integer dueDay;
    private String status;
    private String remark;
    private String createBy;
    private Date createTime;
}
