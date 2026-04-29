package com.spmp.owner.domain.vo;

import lombok.Data;

import java.util.Date;

/**
 * 房产绑定 VO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class PropertyBindingVO {

    /** 绑定ID */
    private Long id;

    /** 业主ID */
    private Long ownerId;

    /** 房屋ID */
    private Long houseId;

    /** 小区ID（通过 BaseApi 查询） */
    private Long communityId;

    /** 小区名称（通过 BaseApi 查询） */
    private String communityName;

    /** 楼栋ID */
    private Long buildingId;

    /** 楼栋名称 */
    private String buildingName;

    /** 单元ID */
    private Long unitId;

    /** 单元名称 */
    private String unitName;

    /** 房屋编号 */
    private String houseCode;

    /** 关系类型 */
    private String relationType;

    /** 绑定时间 */
    private Date bindingTime;

    /** 解绑时间 */
    private Date unbindingTime;

    /** 绑定状态（0-有效 1-已解绑） */
    private Integer status;
}
