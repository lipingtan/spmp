package com.spmp.base.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 单元简要信息 DTO（对外接口）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class UnitBriefDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 单元ID */
    private Long id;

    /** 单元名称 */
    private String unitName;

    /** 单元编号 */
    private String unitCode;

    /** 所属楼栋ID */
    private Long buildingId;

    /** 状态（0-启用 1-停用） */
    private Integer status;
}
