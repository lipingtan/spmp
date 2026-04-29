package com.spmp.owner.service;

import com.spmp.common.result.PageResult;
import com.spmp.owner.domain.dto.H5RegisterDTO;
import com.spmp.owner.domain.dto.OwnerCreateDTO;
import com.spmp.owner.domain.dto.OwnerQueryDTO;
import com.spmp.owner.domain.dto.OwnerStatusDTO;
import com.spmp.owner.domain.dto.OwnerUpdateDTO;
import com.spmp.owner.domain.vo.H5ProfileVO;
import com.spmp.owner.domain.vo.OwnerListVO;
import com.spmp.owner.domain.vo.OwnerVO;

/**
 * 业主服务接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface OwnerService {

    /**
     * 分页查询业主列表（JOIN ow_property_binding + bs_house 实现数据权限过滤）。
     *
     * @param queryDTO 查询条件
     * @return 分页结果（脱敏）
     */
    PageResult<OwnerListVO> listOwners(OwnerQueryDTO queryDTO);

    /**
     * 新增业主（加密手机号/身份证号，校验唯一性，来源=ADMIN，状态=UNCERTIFIED）。
     *
     * @param createDTO 新增参数
     */
    void createOwner(OwnerCreateDTO createDTO);

    /**
     * 查询业主详情（含房产绑定列表、家庭成员列表、认证历史）。
     *
     * @param id 业主ID
     * @return 业主详情 VO
     */
    OwnerVO getOwnerDetail(Long id);

    /**
     * 编辑业主（重新校验手机号/身份证号唯一性，排除自身）。
     *
     * @param id        业主ID
     * @param updateDTO 编辑参数
     */
    void updateOwner(Long id, OwnerUpdateDTO updateDTO);

    /**
     * 删除业主（逻辑删除，校验未完结工单/未缴清账单）。
     *
     * @param id 业主ID
     */
    void deleteOwner(Long id);

    /**
     * 停用/启用业主（停用时保存 previous_status，启用时恢复原状态）。
     *
     * @param id        业主ID
     * @param statusDTO 操作参数
     */
    void changeOwnerStatus(Long id, OwnerStatusDTO statusDTO);

    /**
     * H5 业主注册。
     *
     * @param registerDTO 注册参数
     */
    void register(H5RegisterDTO registerDTO);

    /**
     * 查询 H5 个人信息（含房产列表，脱敏返回）。
     *
     * @param ownerId 业主ID
     * @return H5 个人信息 VO
     */
    H5ProfileVO getProfile(Long ownerId);
}
