package com.spmp.owner.service;

import com.spmp.owner.domain.dto.FamilyMemberCreateDTO;
import com.spmp.owner.domain.vo.FamilyMemberVO;

import java.util.List;

/**
 * 家庭成员服务接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface FamilyMemberService {

    /**
     * 查询业主的家庭成员列表（脱敏返回）。
     *
     * @param ownerId 业主ID
     * @return 家庭成员 VO 列表
     */
    List<FamilyMemberVO> listByOwnerId(Long ownerId);

    /**
     * 添加家庭成员（校验人数上限10人，加密手机号/身份证号，调用 UserApi.createUser 创建用户，关联房产）。
     *
     * @param ownerId   业主ID
     * @param createDTO 添加参数
     */
    void addFamilyMember(Long ownerId, FamilyMemberCreateDTO createDTO);

    /**
     * 删除家庭成员（逻辑删除，解除房产绑定，保留 sys_user 账号）。
     *
     * @param ownerId 业主ID
     * @param id      家庭成员ID
     */
    void deleteFamilyMember(Long ownerId, Long id);
}
