package com.spmp.base.domain.dto;

import lombok.Data;

import java.util.Date;

/**
 * 单元分页查询结果 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class UnitPageDTO {

    /** 主键ID */
    private Long id;

    /** 单元名称 */
    private String unitName;

    /** 单元编号 */
    private String unitCode;

    /** 所属楼栋ID */
    private Long buildingId;

    /** 所属楼栋名称（关联查询） */
    private String buildingName;

    /** 状态（0-启用 1-停用） */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;
}
