package com.spmp.billing.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.spmp.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录表实体。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bl_payment")
public class PaymentDO extends BaseEntity {

    private String paymentNo;

    private BigDecimal totalAmount;

    private String payMethod;

    private String status;

    private Long ownerId;

    private String ownerName;

    private LocalDateTime paidTime;

    private LocalDateTime expireTime;
}
