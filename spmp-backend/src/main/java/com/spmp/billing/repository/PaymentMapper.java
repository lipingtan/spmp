package com.spmp.billing.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spmp.billing.domain.entity.PaymentDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付记录 Mapper 接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Mapper
public interface PaymentMapper extends BaseMapper<PaymentDO> {
}
