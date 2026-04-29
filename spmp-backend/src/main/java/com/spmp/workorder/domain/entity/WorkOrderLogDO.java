package com.spmp.workorder.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 工单操作日志表实体。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@TableName("wo_work_order_log")
public class WorkOrderLogDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private String action;

    private String fromStatus;

    private String toStatus;

    private Long operatorId;

    private String operatorName;

    private String operatorType;

    private String remark;

    private LocalDateTime operateTime;

    private LocalDateTime createTime;
}
