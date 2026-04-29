package com.spmp.billing.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.spmp.billing.domain.dto.H5PaymentCreateDTO;
import com.spmp.billing.domain.vo.PaymentVO;

/**
 * 支付服务接口。
 */
public interface PaymentService {

    Long createPayment(H5PaymentCreateDTO dto, Long ownerId);

    void paymentCallback(Long paymentId, String callbackSign);

    PaymentVO getPaymentDetail(Long id, Long ownerId);

    IPage<PaymentVO> listPaymentHistory(Long ownerId, Integer pageNum, Integer pageSize);
}
