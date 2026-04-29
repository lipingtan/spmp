package com.spmp.base.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 级联树节点 DTO（对外接口）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class CascadeTreeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 节点ID */
    private Long id;

    /** 节点名称 */
    private String name;

    /** 节点编码 */
    private String code;

    /** 节点类型（BUILDING/UNIT/HOUSE） */
    private String type;

    /** 子节点列表 */
    private List<CascadeTreeDTO> children;
}
