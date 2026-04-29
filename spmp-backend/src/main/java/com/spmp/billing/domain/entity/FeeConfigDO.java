package com.spmp.billing.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.spmp.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 费用配置表实体。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bl_fee_config")
public class FeeConfigDO extends BaseEntity {

    private Long communityId;

    private Long buildingId;

    private String feeType;

    private String billingMethod;

    private BigDecimal unitPrice;

    private Integer dueDay;

    private String status;

    private String remark;
}
