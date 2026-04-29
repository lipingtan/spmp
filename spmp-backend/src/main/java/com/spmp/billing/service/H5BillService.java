package com.spmp.billing.service;

import com.spmp.billing.domain.dto.H5BillQueryDTO;
import com.spmp.billing.domain.dto.H5PaymentCreateDTO;
import com.spmp.billing.domain.vo.H5BillDetailVO;
import com.spmp.billing.domain.vo.H5BillListVO;
import com.spmp.billing.domain.vo.PaymentVO;
import com.spmp.common.result.PageResult;

/**
 * H5 业主端缴费服务接口。
 * <p>
 * 封装 H5 端所有账单和支付相关业务逻辑，Controller 层只依赖本接口，
 * 不直接操作 Mapper 或跨模块 Repository。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface H5BillService {

    /**
     * 查询当前业主的账单列表（分页）。
     *
     * @param ownerId  当前业主 ID
     * @param queryDTO 查询条件（状态、费用类型、分页参数）
     * @return 分页账单列表
     */
    PageResult<H5BillListVO> listMyBills(Long ownerId, H5BillQueryDTO queryDTO);

    /**
     * 查询账单详情（含支付记录、减免记录）。
     * <p>
     * 同时校验账单归属权，非本人账单抛出业务异常。
     *
     * @param id      账单 ID
     * @param ownerId 当前业主 ID（用于归属权校验）
     * @return 账单详情 VO
     */
    H5BillDetailVO getBillDetail(Long id, Long ownerId);

    /**
     * 获取当前登录用户对应的业主 ID。
     * <p>
     * 通过 SecurityUtils 获取 userId，再查询 ow_owner 表得到 ownerId。
     * 若当前用户未绑定业主信息则抛出业务异常。
     *
     * @return 业主 ID
     */
    Long getCurrentOwnerId();

    /**
     * 创建支付单。
     *
     * @param dto     支付创建参数（账单 ID 列表、支付方式）
     * @param ownerId 当前业主 ID
     * @return 支付单 ID
     */
    Long createPayment(H5PaymentCreateDTO dto, Long ownerId);

    /**
     * 支付回调（模拟支付渠道回调，更新支付单和账单状态）。
     *
     * @param paymentId 支付单 ID
     */
    void paymentCallback(Long paymentId, String callbackSign);

    /**
     * 查询支付单详情。
     *
     * @param id 支付单 ID
     * @return 支付单 VO
     */
    PaymentVO getPaymentDetail(Long id, Long ownerId);

    /**
     * 查询当前业主的支付历史（分页，仅返回成功状态）。
     *
     * @param ownerId  当前业主 ID
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页支付历史
     */
    PageResult<PaymentVO> listPaymentHistory(Long ownerId, Integer pageNum, Integer pageSize);
}
