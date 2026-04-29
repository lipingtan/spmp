package com.spmp.billing.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spmp.billing.domain.entity.UrgeRecordDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 催收记录 Mapper 接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Mapper
public interface BillUrgeRecordMapper extends BaseMapper<UrgeRecordDO> {

    /**
     * 查询账单催收记录（兼容历史表结构：无 update_time 字段）。
     *
     * @param billId 账单ID
     * @return 催收记录列表
     */
    @Select("SELECT id, bill_id, bill_no, urge_type, urge_user_id, urge_user_name, urge_time, feedback, " +
            "create_time " +
            "FROM bl_urge_record " +
            "WHERE bill_id = #{billId} " +
            "ORDER BY urge_time DESC")
    List<UrgeRecordDO> selectByBillId(@Param("billId") Long billId);
}
