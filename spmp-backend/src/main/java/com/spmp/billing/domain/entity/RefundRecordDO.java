package com.spmp.billing.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 退款记录表实体。
 * <p>
 * 与 {@code bl_refund_record} DDL 对齐：该表无逻辑删除、无 update_time / create_by 等通用审计字段，
 * 故不继承 {@link com.spmp.common.domain.BaseEntity}，避免 INSERT 生成多余列导致 SQL 异常（前端表现为「系统繁忙」）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@TableName("bl_refund_record")
public class RefundRecordDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long billId;

    private String billNo;

    private BigDecimal refundAmount;

    private String refundReason;

    private Long operatorId;

    private String operatorName;

    private LocalDateTime refundTime;

    /** 对应列 create_time，插入前由业务层赋值 */
    private Date createTime;
}
