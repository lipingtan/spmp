package com.spmp.base.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.spmp.base.domain.dto.HousePageDTO;
import com.spmp.base.domain.dto.HouseQueryDTO;
import com.spmp.base.domain.entity.HouseDO;
import com.spmp.common.security.DataPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 房屋 Mapper 接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Mapper
public interface HouseMapper extends BaseMapper<HouseDO> {

    /**
     * 分页查询房屋（数据权限过滤）。
     * <p>
     * 房屋查询 SQL 需 JOIN unit → building → community 获取各级 ID。
     * 使用 SQL 别名指定字段。
     *
     * @param page     分页参数
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @DataPermission(buildingField = "bd.id")
    IPage<HousePageDTO> selectHousePage(IPage<?> page, @Param("query") HouseQueryDTO queryDTO);
}
