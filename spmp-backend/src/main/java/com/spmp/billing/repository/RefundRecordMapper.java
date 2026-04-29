package com.spmp.billing.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spmp.billing.domain.entity.RefundRecordDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 退款记录 Mapper 接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Mapper
public interface RefundRecordMapper extends BaseMapper<RefundRecordDO> {

    /**
     * 查询账单退款记录（兼容历史表结构）。
     *
     * @param billId 账单ID
     * @return 退款记录列表
     */
    @Select("SELECT id, bill_id, bill_no, refund_amount, refund_reason, operator_id, operator_name, refund_time, create_time " +
            "FROM bl_refund_record " +
            "WHERE bill_id = #{billId} " +
            "ORDER BY refund_time DESC")
    List<RefundRecordDO> selectByBillId(@Param("billId") Long billId);
}
