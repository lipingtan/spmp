package com.spmp.base.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.spmp.base.domain.dto.DistrictPageDTO;
import com.spmp.base.domain.dto.DistrictQueryDTO;
import com.spmp.base.domain.entity.DistrictDO;
import com.spmp.common.security.DataPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 片区 Mapper 接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Mapper
public interface DistrictMapper extends BaseMapper<DistrictDO> {

    /**
     * 分页查询片区（数据权限过滤）。
     * <p>
     * 片区表的 id 即为 area_id，DataPermissionInterceptor 会追加 id IN (...) 条件。
     *
     * @param page     分页参数
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @DataPermission(areaField = "id")
    IPage<DistrictPageDTO> selectDistrictPage(IPage<?> page, @Param("query") DistrictQueryDTO queryDTO);

    /**
     * 查询当前最大片区编码。
     *
     * @return 最大编码，如 DIS000003
     */
    @Select("SELECT MAX(district_code) FROM bs_district")
    String selectMaxCode();
}
