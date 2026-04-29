package com.spmp.base.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 完整树节点 VO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class CascadeTreeVO {

    /** 节点ID */
    private Long id;

    /** 节点名称 */
    private String name;

    /** 节点编码 */
    private String code;

    /** 节点类型（BUILDING/UNIT/HOUSE） */
    private String type;

    /** 子节点列表 */
    private List<CascadeTreeVO> children;
}
