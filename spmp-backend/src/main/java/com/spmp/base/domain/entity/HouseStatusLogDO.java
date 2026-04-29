package com.spmp.base.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 房屋状态变更历史表实体。
 * <p>
 * 不继承 BaseEntity，无 create_by/update_by 等公共字段。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@TableName("bs_house_status_log")
public class HouseStatusLogDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 房屋ID */
    private Long houseId;

    /** 变更前状态 */
    private String oldStatus;

    /** 变更后状态 */
    private String newStatus;

    /** 变更时间 */
    private Date changeTime;

    /** 操作人ID */
    private Long operatorId;
}
