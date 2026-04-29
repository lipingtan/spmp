package com.spmp.base.api;

import com.spmp.base.api.dto.BuildingBriefDTO;
import com.spmp.base.api.dto.CascadeTreeDTO;
import com.spmp.base.api.dto.CommunityBriefDTO;
import com.spmp.base.api.dto.HouseBriefDTO;
import com.spmp.base.api.dto.UnitBriefDTO;

import java.util.List;

/**
 * 基础数据对外 API 接口。
 * <p>
 * 供 user、owner、workorder、billing、notice、access 等模块注入调用。
 * 单体应用内直接注入，禁止直接注入 base 模块的 Service 实现类。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface BaseApi {

    /**
     * 查询小区信息。
     *
     * @param communityId 小区ID
     * @return 小区简要信息，不存在时返回 null
     */
    CommunityBriefDTO getCommunityById(Long communityId);

    /**
     * 查询楼栋信息。
     *
     * @param buildingId 楼栋ID
     * @return 楼栋简要信息，不存在时返回 null
     */
    BuildingBriefDTO getBuildingById(Long buildingId);

    /**
     * 查询单元信息。
     *
     * @param unitId 单元ID
     * @return 单元简要信息，不存在时返回 null
     */
    UnitBriefDTO getUnitById(Long unitId);

    /**
     * 查询房屋信息。
     *
     * @param houseId 房屋ID
     * @return 房屋简要信息，不存在时返回 null
     */
    HouseBriefDTO getHouseById(Long houseId);

    /**
     * 查询所有启用状态的片区ID列表。
     *
     * @return 启用片区ID列表
     */
    List<Long> listDistrictIds();

    /**
     * 按片区查小区列表（启用状态）。
     *
     * @param districtId 片区ID
     * @return 小区简要信息列表
     */
    List<CommunityBriefDTO> listCommunitiesByDistrictId(Long districtId);

    /**
     * 按小区查楼栋列表（启用状态）。
     *
     * @param communityId 小区ID
     * @return 楼栋简要信息列表
     */
    List<BuildingBriefDTO> listBuildingsByCommunityId(Long communityId);

    /**
     * 获取完整树形结构（小区下楼栋→单元→房屋）。
     *
     * @param communityId 小区ID
     * @return 级联树节点列表
     */
    List<CascadeTreeDTO> getCascadeTree(Long communityId);

    /**
     * 更新房屋状态。
     * <p>
     * 供 owner 模块房产绑定/解绑时调用。
     *
     * @param houseId 房屋ID
     * @param status  目标状态（VACANT / OCCUPIED / RENTED / RENOVATING）
     */
    void updateHouseStatus(Long houseId, String status);
}
