package com.spmp.owner.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.spmp.common.exception.BusinessException;
import com.spmp.common.util.EncryptUtils;
import com.spmp.owner.constant.OwnerConstants;
import com.spmp.owner.constant.OwnerErrorCode;
import com.spmp.owner.constant.FamilyRelation;
import com.spmp.owner.constant.RelationType;
import com.spmp.owner.domain.dto.FamilyMemberCreateDTO;
import com.spmp.owner.domain.entity.FamilyMemberDO;
import com.spmp.owner.domain.entity.PropertyBindingDO;
import com.spmp.owner.domain.vo.FamilyMemberVO;
import com.spmp.owner.repository.FamilyMemberMapper;
import com.spmp.owner.repository.PropertyBindingMapper;
import com.spmp.owner.service.FamilyMemberService;
import com.spmp.user.api.UserApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 家庭成员服务实现类。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FamilyMemberServiceImpl implements FamilyMemberService {

    private final FamilyMemberMapper familyMemberMapper;
    private final PropertyBindingMapper propertyBindingMapper;
    private final EncryptUtils encryptUtils;
    private final UserApi userApi;

    @Override
    public List<FamilyMemberVO> listByOwnerId(Long ownerId) {
        LambdaQueryWrapper<FamilyMemberDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FamilyMemberDO::getOwnerId, ownerId)
                .orderByDesc(FamilyMemberDO::getCreateTime);

        List<FamilyMemberDO> members = familyMemberMapper.selectList(wrapper);
        return members.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFamilyMember(Long ownerId, FamilyMemberCreateDTO createDTO) {
        // 校验家庭关系类型
        if (!FamilyRelation.isValid(createDTO.getRelation())) {
            throw new BusinessException(OwnerErrorCode.FAMILY_MEMBER_RELATION_INVALID.getCode(),
                    OwnerErrorCode.FAMILY_MEMBER_RELATION_INVALID.getMessage());
        }

        // 校验人数上限（含业主本人）
        LambdaQueryWrapper<FamilyMemberDO> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(FamilyMemberDO::getOwnerId, ownerId);
        Long memberCount = familyMemberMapper.selectCount(countWrapper);
        // 含业主本人，所以上限判断为 memberCount + 1 >= FAMILY_MEMBER_LIMIT
        if (memberCount + 1 >= OwnerConstants.FAMILY_MEMBER_LIMIT) {
            throw new BusinessException(OwnerErrorCode.FAMILY_MEMBER_LIMIT_EXCEEDED.getCode(),
                    OwnerErrorCode.FAMILY_MEMBER_LIMIT_EXCEEDED.getMessage());
        }

        // 调用 UserApi.createUser 创建用户
        com.spmp.user.api.dto.UserCreateDTO userCreateDTO = new com.spmp.user.api.dto.UserCreateDTO();
        userCreateDTO.setUsername(createDTO.getPhone());
        userCreateDTO.setPassword(createDTO.getPhone().substring(5)); // 手机号后6位
        userCreateDTO.setRoleCode("owner");
        Long userId = userApi.createUser(userCreateDTO);

        // 加密手机号和身份证号
        String encryptedPhone = encryptUtils.encrypt(createDTO.getPhone());
        String encryptedIdCard = encryptUtils.encrypt(createDTO.getIdCard());

        // 创建家庭成员记录
        FamilyMemberDO memberDO = new FamilyMemberDO();
        memberDO.setOwnerId(ownerId);
        memberDO.setUserId(userId);
        memberDO.setMemberName(createDTO.getMemberName());
        memberDO.setPhone(encryptedPhone);
        memberDO.setIdCard(encryptedIdCard);
        memberDO.setRelation(createDTO.getRelation());
        familyMemberMapper.insert(memberDO);

        // 关联业主所有有效房产（为家庭成员创建 FAMILY 类型的绑定记录）
        LambdaQueryWrapper<PropertyBindingDO> bindingWrapper = new LambdaQueryWrapper<>();
        bindingWrapper.eq(PropertyBindingDO::getOwnerId, ownerId)
                .eq(PropertyBindingDO::getStatus, 0);
        List<PropertyBindingDO> ownerBindings = propertyBindingMapper.selectList(bindingWrapper);

        for (PropertyBindingDO ownerBinding : ownerBindings) {
            PropertyBindingDO familyBinding = new PropertyBindingDO();
            familyBinding.setOwnerId(memberDO.getId()); // 使用家庭成员ID作为绑定的ownerId
            familyBinding.setHouseId(ownerBinding.getHouseId());
            familyBinding.setRelationType(RelationType.FAMILY.getCode());
            familyBinding.setBindingTime(new Date());
            familyBinding.setStatus(0);
            propertyBindingMapper.insert(familyBinding);
        }

        log.info("添加家庭成员成功，memberId={}, ownerId={}, 关联房产数={}",
                memberDO.getId(), ownerId, ownerBindings.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFamilyMember(Long ownerId, Long id) {
        // 查询家庭成员
        FamilyMemberDO memberDO = familyMemberMapper.selectById(id);
        if (memberDO == null) {
            throw new BusinessException(OwnerErrorCode.FAMILY_MEMBER_NOT_FOUND.getCode(),
                    OwnerErrorCode.FAMILY_MEMBER_NOT_FOUND.getMessage());
        }

        // 校验成员属于当前业主
        if (!memberDO.getOwnerId().equals(ownerId)) {
            throw new BusinessException(OwnerErrorCode.FAMILY_MEMBER_NOT_BELONG.getCode(),
                    OwnerErrorCode.FAMILY_MEMBER_NOT_BELONG.getMessage());
        }

        // 逻辑删除家庭成员
        familyMemberMapper.deleteById(id);

        // 解除该成员的房产绑定记录（删除 ow_property_binding 中该成员的绑定记录）
        LambdaUpdateWrapper<PropertyBindingDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PropertyBindingDO::getOwnerId, id)
                .eq(PropertyBindingDO::getRelationType, RelationType.FAMILY.getCode())
                .eq(PropertyBindingDO::getStatus, 0)
                .set(PropertyBindingDO::getStatus, 1)
                .set(PropertyBindingDO::getUnbindingTime, new Date());
        propertyBindingMapper.update(null, updateWrapper);

        // 注意：保留 sys_user 账号，不删除
        log.info("删除家庭成员成功，memberId={}, ownerId={}", id, ownerId);
    }

    // ========== 私有方法 ==========

    /**
     * 将 FamilyMemberDO 转换为 FamilyMemberVO（含脱敏）。
     */
    private FamilyMemberVO convertToVO(FamilyMemberDO memberDO) {
        FamilyMemberVO vo = new FamilyMemberVO();
        vo.setId(memberDO.getId());
        vo.setOwnerId(memberDO.getOwnerId());
        vo.setMemberName(memberDO.getMemberName());
        vo.setPhoneMasked(EncryptUtils.mask(encryptUtils.decrypt(memberDO.getPhone()), 3, 4));
        vo.setIdCardMasked(EncryptUtils.mask(encryptUtils.decrypt(memberDO.getIdCard()), 3, 4));
        vo.setRelation(memberDO.getRelation());
        vo.setCreateTime(memberDO.getCreateTime());
        return vo;
    }
}
