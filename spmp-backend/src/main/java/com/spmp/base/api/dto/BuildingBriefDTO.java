package com.spmp.base.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 楼栋简要信息 DTO（对外接口）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class BuildingBriefDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 楼栋ID */
    private Long id;

    /** 楼栋名称 */
    private String buildingName;

    /** 楼栋编号 */
    private String buildingCode;

    /** 所属小区ID */
    private Long communityId;

    /** 楼栋类型 */
    private String buildingType;

    /** 状态（0-启用 1-停用） */
    private Integer status;
}
