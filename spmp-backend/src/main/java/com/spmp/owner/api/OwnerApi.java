package com.spmp.owner.api;

import com.spmp.owner.api.dto.OwnerBriefDTO;

import java.util.List;

/**
 * 业主模块对外 API 接口（供其他模块调用）。
 * <p>
 * 供 workorder、billing、notice、access 等模块注入调用。
 * 单体应用内直接注入，禁止直接注入 owner 模块的 Service 实现类。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface OwnerApi {

    /**
     * 按 ID 查业主。
     *
     * @param ownerId 业主ID
     * @return 业主简要信息，不存在返回 null
     */
    OwnerBriefDTO getOwnerById(Long ownerId);

    /**
     * 按房屋查业主。
     *
     * @param houseId 房屋ID
     * @return 业主简要信息，不存在返回 null
     */
    OwnerBriefDTO getOwnerByHouseId(Long houseId);

    /**
     * 按手机号查业主（传入明文，内部加密后查询）。
     *
     * @param phone 明文手机号
     * @return 业主简要信息，不存在返回 null
     */
    OwnerBriefDTO getOwnerByPhone(String phone);

    /**
     * 按小区查业主列表。
     *
     * @param communityId 小区ID
     * @return 业主列表
     */
    List<OwnerBriefDTO> listOwnersByCommunityId(Long communityId);

    /**
     * 按楼栋查业主列表。
     *
     * @param buildingId 楼栋ID
     * @return 业主列表
     */
    List<OwnerBriefDTO> listOwnersByBuildingId(Long buildingId);

    /**
     * 检查业主是否已认证。
     *
     * @param ownerId 业主ID
     * @return 是否已认证
     */
    boolean checkOwnerCertified(Long ownerId);
}
