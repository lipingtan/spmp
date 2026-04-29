package com.spmp.base.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 小区简要信息 DTO（对外接口）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class CommunityBriefDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 小区ID */
    private Long id;

    /** 小区名称 */
    private String communityName;

    /** 小区编码 */
    private String communityCode;

    /** 小区地址 */
    private String address;

    /** 所属片区ID */
    private Long districtId;

    /** 状态（0-启用 1-停用） */
    private Integer status;
}
