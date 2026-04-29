package com.spmp.billing.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付账单关联表实体。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@TableName("bl_payment_bill")
public class PaymentBillDO {

    private Long id;

    private Long paymentId;

    private Long billId;

    private BigDecimal payAmount;

    private LocalDateTime createTime;
}
