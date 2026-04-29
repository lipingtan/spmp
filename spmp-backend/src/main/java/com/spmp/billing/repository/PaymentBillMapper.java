package com.spmp.billing.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spmp.billing.domain.entity.PaymentBillDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付账单关联 Mapper 接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Mapper
public interface PaymentBillMapper extends BaseMapper<PaymentBillDO> {
}
