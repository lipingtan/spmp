package com.spmp.base.service;

import com.spmp.base.domain.vo.CascadeTreeVO;
import com.spmp.base.domain.vo.CascadeVO;

import java.util.List;

/**
 * 级联查询服务接口。
 * <p>
 * 提供逐级级联查询和完整树查询功能，结果缓存到 Redis。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface CascadeService {

    /**
     * 级联数据查询（按级别逐级查询直接子级）。
     *
     * @param level    级别（DISTRICT/COMMUNITY/BUILDING/UNIT/HOUSE）
     * @param parentId 父级ID（查询片区列表时可为 null）
     * @return 直接子级列表
     */
    List<CascadeVO> getCascadeData(String level, Long parentId);

    /**
     * 完整树查询（小区下楼栋→单元→房屋）。
     *
     * @param communityId 小区ID
     * @return 完整树形结构
     */
    List<CascadeTreeVO> getCascadeTree(Long communityId);
}
