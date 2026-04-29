package com.spmp.base.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.spmp.base.domain.dto.BuildingPageDTO;
import com.spmp.base.domain.dto.BuildingQueryDTO;
import com.spmp.base.domain.entity.BuildingDO;
import com.spmp.common.security.DataPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 楼栋 Mapper 接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Mapper
public interface BuildingMapper extends BaseMapper<BuildingDO> {

    /**
     * 分页查询楼栋（数据权限过滤）。
     * <p>
     * community_id 对应小区级权限，id 对应楼栋级权限。
     * 片区级权限需通过 SQL JOIN bs_community 获取 district_id。
     *
     * @param page     分页参数
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @DataPermission(communityField = "b.community_id", buildingField = "b.id")
    IPage<BuildingPageDTO> selectBuildingPage(IPage<?> page, @Param("query") BuildingQueryDTO queryDTO);
}
