package com.spmp.workorder.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.spmp.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 派发记录表实体。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wo_dispatch_record")
public class DispatchRecordDO extends BaseEntity {

    private Long orderId;

    private Long repairUserId;

    private String repairUserName;

    private String dispatchType;

    private Long dispatcherId;

    private String dispatcherName;

    private String remark;

    private LocalDateTime dispatchTime;
}
