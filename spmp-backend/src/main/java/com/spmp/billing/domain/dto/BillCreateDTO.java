package com.spmp.billing.domain.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 手动创建账单 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class BillCreateDTO {

    @NotNull(message = "业主不能为空")
    private Long ownerId;

    /** 业主姓名（冗余，由调用方填充） */
    private String ownerName;

    @NotNull(message = "房屋不能为空")
    private Long houseId;

    /** 楼栋ID（冗余，由调用方填充） */
    private Long buildingId;

    /** 小区ID（冗余，用于数据权限，由调用方填充） */
    @NotNull(message = "小区不能为空")
    private Long communityId;

    @NotBlank(message = "费用类型不能为空")
    private String feeType;

    @NotBlank(message = "账期不能为空")
    private String billingPeriod;

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    private BigDecimal amount;

    /** 用量（水费/电费/燃气费使用） */
    private BigDecimal usageAmount;

    /** 单价快照 */
    private BigDecimal unitPrice;

    /** 房屋面积快照（物业费使用） */
    private BigDecimal houseArea;

    /** 缴费截止日期 */
    private LocalDate dueDate;

    private String remark;
}
