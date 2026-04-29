package com.spmp.owner.domain.vo;

import lombok.Data;

import java.util.Date;

/**
 * 认证申请 VO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class CertificationVO {

    /** 认证ID */
    private Long id;

    /** 业主ID */
    private Long ownerId;

    /** 业主姓名 */
    private String ownerName;

    /** 房屋ID */
    private Long houseId;

    /** 小区名称 */
    private String communityName;

    /** 楼栋名称 */
    private String buildingName;

    /** 单元名称 */
    private String unitName;

    /** 房屋编号 */
    private String houseCode;

    /**
     * 房屋名称（与 houseCode 相同，供 H5 端使用）。
     */
    private String houseName;

    /** 认证状态（certStatus 的别名，供 H5 端使用） */
    private String status;

    /** 认证状态（原始字段，供 PC 端使用） */
    private String certStatus;

    /** 申请时间 */
    private Date applyTime;

    /** 审批时间 */
    private Date approveTime;

    /** 审批人姓名 */
    private String approverName;

    /** 驳回原因 */
    private String rejectReason;
}
