package com.spmp.base.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.spmp.base.domain.dto.CommunityPageDTO;
import com.spmp.base.domain.dto.CommunityQueryDTO;
import com.spmp.base.domain.entity.CommunityDO;
import com.spmp.common.security.DataPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 小区 Mapper 接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Mapper
public interface CommunityMapper extends BaseMapper<CommunityDO> {

    /**
     * 分页查询小区（数据权限过滤）。
     * <p>
     * district_id 对应片区级权限，id 对应小区级权限。
     *
     * @param page     分页参数
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @DataPermission(areaField = "district_id", communityField = "id")
    IPage<CommunityPageDTO> selectCommunityPage(IPage<?> page, @Param("query") CommunityQueryDTO queryDTO);

    /**
     * 查询当前最大小区编码。
     *
     * @return 最大编码，如 COM000005
     */
    @Select("SELECT MAX(community_code) FROM bs_community")
    String selectMaxCode();

    /**
     * 统计指定片区下的小区数量。
     *
     * @param districtId 片区ID
     * @return 小区数量
     */
    @Select("SELECT COUNT(*) FROM bs_community WHERE district_id = #{districtId} AND del_flag = 0")
    int countByDistrictId(@Param("districtId") Long districtId);
}
