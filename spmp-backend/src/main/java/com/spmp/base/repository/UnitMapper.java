package com.spmp.base.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.spmp.base.domain.dto.UnitPageDTO;
import com.spmp.base.domain.dto.UnitQueryDTO;
import com.spmp.base.domain.entity.UnitDO;
import com.spmp.common.security.DataPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 单元 Mapper 接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Mapper
public interface UnitMapper extends BaseMapper<UnitDO> {

    /**
     * 分页查询单元（数据权限过滤）。
     * <p>
     * 通过 JOIN bs_building 获取 community_id 和 building_id。
     *
     * @param page     分页参数
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @DataPermission(buildingField = "u.building_id")
    IPage<UnitPageDTO> selectUnitPage(IPage<?> page, @Param("query") UnitQueryDTO queryDTO);
}
