package com.spmp.owner.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spmp.common.exception.BusinessException;
import com.spmp.common.result.PageResult;
import com.spmp.common.util.EncryptUtils;
import com.spmp.owner.constant.OwnerErrorCode;
import com.spmp.owner.constant.OwnerSource;
import com.spmp.owner.constant.OwnerStatus;
import com.spmp.owner.domain.dto.H5RegisterDTO;
import com.spmp.owner.domain.dto.OwnerCreateDTO;
import com.spmp.owner.domain.dto.OwnerPageDTO;
import com.spmp.owner.domain.dto.OwnerQueryDTO;
import com.spmp.owner.domain.dto.OwnerStatusDTO;
import com.spmp.owner.domain.dto.OwnerUpdateDTO;
import com.spmp.owner.domain.entity.OwnerDO;
import com.spmp.owner.domain.vo.CertificationVO;
import com.spmp.owner.domain.vo.FamilyMemberVO;
import com.spmp.owner.domain.vo.H5ProfileVO;
import com.spmp.owner.domain.vo.OwnerListVO;
import com.spmp.owner.domain.vo.OwnerVO;
import com.spmp.owner.domain.vo.PropertyBindingVO;
import com.spmp.owner.repository.OwnerMapper;
import com.spmp.owner.service.CertificationService;
import com.spmp.owner.service.FamilyMemberService;
import com.spmp.owner.service.OwnerService;
import com.spmp.owner.service.PropertyBindingService;
import com.spmp.user.api.UserApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 业主服务实现类。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class OwnerServiceImpl implements OwnerService {

    private final OwnerMapper ownerMapper;
    private final EncryptUtils encryptUtils;
    private final UserApi userApi;
    private final PropertyBindingService propertyBindingService;
    private final FamilyMemberService familyMemberService;
    private final CertificationService certificationService;

    public OwnerServiceImpl(OwnerMapper ownerMapper,
                            EncryptUtils encryptUtils,
                            UserApi userApi,
                            @Lazy PropertyBindingService propertyBindingService,
                            @Lazy FamilyMemberService familyMemberService,
                            @Lazy CertificationService certificationService) {
        this.ownerMapper = ownerMapper;
        this.encryptUtils = encryptUtils;
        this.userApi = userApi;
        this.propertyBindingService = propertyBindingService;
        this.familyMemberService = familyMemberService;
        this.certificationService = certificationService;
    }

    @Override
    public PageResult<OwnerListVO> listOwners(OwnerQueryDTO queryDTO) {
        // 手机号搜索时先加密再传入 Mapper
        if (StringUtils.hasText(queryDTO.getPhone())) {
            queryDTO.setPhone(encryptUtils.encrypt(queryDTO.getPhone()));
        }

        IPage<OwnerPageDTO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<OwnerPageDTO> result = ownerMapper.selectOwnerPage(page, queryDTO);

        // 转换为 VO 并脱敏
        List<OwnerListVO> voList = result.getRecords().stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());

        // 构建分页结果
        PageResult<OwnerListVO> pageResult = new PageResult<>();
        pageResult.setCode(200);
        pageResult.setMessage("success");
        pageResult.setData(voList);
        pageResult.setTotal(result.getTotal());
        pageResult.setPageNum((int) result.getCurrent());
        pageResult.setPageSize((int) result.getSize());
        return pageResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOwner(OwnerCreateDTO createDTO) {
        // 加密手机号和身份证号
        String encryptedPhone = encryptUtils.encrypt(createDTO.getPhone());
        String encryptedIdCard = encryptUtils.encrypt(createDTO.getIdCard());

        // 校验手机号唯一性
        OwnerDO existByPhone = ownerMapper.selectByEncryptedPhone(encryptedPhone);
        if (existByPhone != null) {
            throw new BusinessException(OwnerErrorCode.OWNER_PHONE_DUPLICATE.getCode(),
                    OwnerErrorCode.OWNER_PHONE_DUPLICATE.getMessage());
        }

        // 校验身份证号唯一性
        List<OwnerDO> existByIdCard = ownerMapper.selectByEncryptedIdCard(encryptedIdCard);
        if (existByIdCard != null && !existByIdCard.isEmpty()) {
            throw new BusinessException(OwnerErrorCode.OWNER_ID_CARD_DUPLICATE.getCode(),
                    OwnerErrorCode.OWNER_ID_CARD_DUPLICATE.getMessage());
        }

        // 构建实体
        OwnerDO ownerDO = new OwnerDO();
        ownerDO.setOwnerName(createDTO.getOwnerName());
        ownerDO.setPhone(encryptedPhone);
        ownerDO.setIdCard(encryptedIdCard);
        ownerDO.setGender(createDTO.getGender() != null ? createDTO.getGender() : 0);
        ownerDO.setAvatar(createDTO.getAvatar());
        ownerDO.setEmail(createDTO.getEmail());
        ownerDO.setEmergencyContact(createDTO.getEmergencyContact());
        if (StringUtils.hasText(createDTO.getEmergencyPhone())) {
            ownerDO.setEmergencyPhone(encryptUtils.encrypt(createDTO.getEmergencyPhone()));
        }
        ownerDO.setOwnerSource(OwnerSource.ADMIN.getCode());
        ownerDO.setOwnerStatus(OwnerStatus.UNCERTIFIED.getCode());
        ownerDO.setUserId(null);

        ownerMapper.insert(ownerDO);
        log.info("新增业主成功，id={}, name={}", ownerDO.getId(), ownerDO.getOwnerName());
    }

    @Override
    public OwnerVO getOwnerDetail(Long id) {
        OwnerDO ownerDO = ownerMapper.selectById(id);
        if (ownerDO == null) {
            throw new BusinessException(OwnerErrorCode.OWNER_NOT_FOUND.getCode(),
                    OwnerErrorCode.OWNER_NOT_FOUND.getMessage());
        }

        OwnerVO vo = convertToOwnerVO(ownerDO);

        // 查询关联数据
        List<PropertyBindingVO> bindings = propertyBindingService.listByOwnerId(id);
        List<FamilyMemberVO> members = familyMemberService.listByOwnerId(id);
        List<CertificationVO> certifications = certificationService.listByOwnerId(id);

        vo.setPropertyBindings(bindings);
        vo.setFamilyMembers(members);
        vo.setCertifications(certifications);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOwner(Long id, OwnerUpdateDTO updateDTO) {
        OwnerDO ownerDO = ownerMapper.selectById(id);
        if (ownerDO == null) {
            throw new BusinessException(OwnerErrorCode.OWNER_NOT_FOUND.getCode(),
                    OwnerErrorCode.OWNER_NOT_FOUND.getMessage());
        }

        String encryptedPhone = encryptUtils.encrypt(updateDTO.getPhone());
        String encryptedIdCard = encryptUtils.encrypt(updateDTO.getIdCard());

        // 校验手机号唯一性（排除自身）
        OwnerDO existByPhone = ownerMapper.selectByEncryptedPhone(encryptedPhone);
        if (existByPhone != null && !existByPhone.getId().equals(id)) {
            throw new BusinessException(OwnerErrorCode.OWNER_PHONE_DUPLICATE.getCode(),
                    OwnerErrorCode.OWNER_PHONE_DUPLICATE.getMessage());
        }

        // 校验身份证号唯一性（排除自身）
        List<OwnerDO> existByIdCard = ownerMapper.selectByEncryptedIdCard(encryptedIdCard);
        if (existByIdCard != null) {
            for (OwnerDO exist : existByIdCard) {
                if (!exist.getId().equals(id)) {
                    throw new BusinessException(OwnerErrorCode.OWNER_ID_CARD_DUPLICATE.getCode(),
                            OwnerErrorCode.OWNER_ID_CARD_DUPLICATE.getMessage());
                }
            }
        }

        // 更新字段
        ownerDO.setOwnerName(updateDTO.getOwnerName());
        ownerDO.setPhone(encryptedPhone);
        ownerDO.setIdCard(encryptedIdCard);
        ownerDO.setGender(updateDTO.getGender());
        ownerDO.setAvatar(updateDTO.getAvatar());
        ownerDO.setEmail(updateDTO.getEmail());
        ownerDO.setEmergencyContact(updateDTO.getEmergencyContact());
        if (StringUtils.hasText(updateDTO.getEmergencyPhone())) {
            ownerDO.setEmergencyPhone(encryptUtils.encrypt(updateDTO.getEmergencyPhone()));
        } else {
            ownerDO.setEmergencyPhone(null);
        }

        ownerMapper.updateById(ownerDO);
        log.info("编辑业主成功，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOwner(Long id) {
        OwnerDO ownerDO = ownerMapper.selectById(id);
        if (ownerDO == null) {
            throw new BusinessException(OwnerErrorCode.OWNER_NOT_FOUND.getCode(),
                    OwnerErrorCode.OWNER_NOT_FOUND.getMessage());
        }

        // TODO: 删除前置校验 — workorder 和 billing 模块还没实现，暂时跳过
        // 待 workorder 模块实现后，调用 WorkOrderApi 检查未完结工单
        // if (workOrderApi.hasUnfinishedOrders(id)) {
        //     throw new BusinessException(OwnerErrorCode.OWNER_HAS_PENDING_WORKORDER.getCode(),
        //             OwnerErrorCode.OWNER_HAS_PENDING_WORKORDER.getMessage());
        // }
        // 待 billing 模块实现后，调用 BillingApi 检查未缴清账单
        // if (billingApi.hasUnpaidBills(id)) {
        //     throw new BusinessException(OwnerErrorCode.OWNER_HAS_UNPAID_BILL.getCode(),
        //             OwnerErrorCode.OWNER_HAS_UNPAID_BILL.getMessage());
        // }

        ownerMapper.deleteById(id);
        log.info("删除业主成功，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeOwnerStatus(Long id, OwnerStatusDTO statusDTO) {
        OwnerDO ownerDO = ownerMapper.selectById(id);
        if (ownerDO == null) {
            throw new BusinessException(OwnerErrorCode.OWNER_NOT_FOUND.getCode(),
                    OwnerErrorCode.OWNER_NOT_FOUND.getMessage());
        }

        String action = statusDTO.getAction();
        if ("DISABLE".equals(action)) {
            if (OwnerStatus.DISABLED.getCode().equals(ownerDO.getOwnerStatus())) {
                throw new BusinessException(OwnerErrorCode.OWNER_ALREADY_DISABLED.getCode(),
                        OwnerErrorCode.OWNER_ALREADY_DISABLED.getMessage());
            }
            // 保存当前状态到 previousStatus，设置为 DISABLED
            ownerDO.setPreviousStatus(ownerDO.getOwnerStatus());
            ownerDO.setOwnerStatus(OwnerStatus.DISABLED.getCode());
        } else if ("ENABLE".equals(action)) {
            if (!OwnerStatus.DISABLED.getCode().equals(ownerDO.getOwnerStatus())) {
                throw new BusinessException(OwnerErrorCode.OWNER_NOT_DISABLED.getCode(),
                        OwnerErrorCode.OWNER_NOT_DISABLED.getMessage());
            }
            // 从 previousStatus 恢复
            String previousStatus = ownerDO.getPreviousStatus();
            ownerDO.setOwnerStatus(previousStatus != null ? previousStatus : OwnerStatus.UNCERTIFIED.getCode());
            ownerDO.setPreviousStatus(null);
        } else {
            throw new BusinessException(OwnerErrorCode.OWNER_NOT_FOUND.getCode(), "无效的操作类型: " + action);
        }

        ownerMapper.updateById(ownerDO);
        log.info("业主状态变更成功，id={}, action={}", id, action);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(H5RegisterDTO registerDTO) {
        // 调用 UserApi.createUser 创建 sys_user
        com.spmp.user.api.dto.UserCreateDTO userCreateDTO = new com.spmp.user.api.dto.UserCreateDTO();
        userCreateDTO.setUsername(registerDTO.getPhone());
        userCreateDTO.setPassword(registerDTO.getPassword());
        userCreateDTO.setRoleCode("owner");
        Long userId = userApi.createUser(userCreateDTO);

        // 加密手机号后查询是否已存在（管理端预录入）
        String encryptedPhone = encryptUtils.encrypt(registerDTO.getPhone());
        OwnerDO existOwner = ownerMapper.selectByEncryptedPhone(encryptedPhone);

        if (existOwner != null) {
            // 已存在，仅关联 user_id（不更新其他字段，避免 EncryptTypeHandler 解密后的明文被写回）
            OwnerDO updateDO = new OwnerDO();
            updateDO.setId(existOwner.getId());
            updateDO.setUserId(userId);
            ownerMapper.updateById(updateDO);
            log.info("H5 注册匹配预录入业主，ownerId={}, userId={}", existOwner.getId(), userId);
        } else {
            // 不存在，创建新业主
            OwnerDO ownerDO = new OwnerDO();
            ownerDO.setUserId(userId);
            ownerDO.setOwnerName(registerDTO.getOwnerName());
            ownerDO.setPhone(encryptedPhone);
            if (StringUtils.hasText(registerDTO.getIdCard())) {
                ownerDO.setIdCard(encryptUtils.encrypt(registerDTO.getIdCard()));
            } else {
                ownerDO.setIdCard("");
            }
            ownerDO.setGender(0);
            ownerDO.setOwnerSource(OwnerSource.H5.getCode());
            ownerDO.setOwnerStatus(OwnerStatus.UNCERTIFIED.getCode());
            ownerMapper.insert(ownerDO);
            log.info("H5 注册创建新业主，ownerId={}, userId={}", ownerDO.getId(), userId);
        }
    }

    @Override
    public H5ProfileVO getProfile(Long ownerId) {
        OwnerDO ownerDO = ownerMapper.selectById(ownerId);
        if (ownerDO == null) {
            throw new BusinessException(OwnerErrorCode.OWNER_NOT_FOUND.getCode(),
                    OwnerErrorCode.OWNER_NOT_FOUND.getMessage());
        }

        H5ProfileVO vo = new H5ProfileVO();
        vo.setId(ownerDO.getId());
        vo.setOwnerName(ownerDO.getOwnerName());
        vo.setPhoneMasked(EncryptUtils.mask(encryptUtils.decrypt(ownerDO.getPhone()), 3, 4));
        if (StringUtils.hasText(ownerDO.getIdCard())) {
            vo.setIdCardMasked(EncryptUtils.mask(encryptUtils.decrypt(ownerDO.getIdCard()), 3, 4));
        }
        vo.setGender(ownerDO.getGender());
        vo.setAvatar(ownerDO.getAvatar());
        vo.setEmail(ownerDO.getEmail());
        vo.setOwnerStatus(ownerDO.getOwnerStatus());

        // 查询房产列表
        List<PropertyBindingVO> properties = propertyBindingService.listByOwnerId(ownerId);
        vo.setProperties(properties);

        return vo;
    }

    // ========== 私有方法 ==========

    /**
     * 将 OwnerPageDTO 转换为 OwnerListVO（含脱敏）。
     */
    private OwnerListVO convertToListVO(OwnerPageDTO pageDTO) {
        OwnerListVO vo = new OwnerListVO();
        vo.setId(pageDTO.getId());
        vo.setOwnerName(pageDTO.getOwnerName());
        // 解密后脱敏（防御性处理：空值或解密失败时返回 "***"）
        vo.setPhoneMasked(safeDecryptAndMask(pageDTO.getPhone(), 3, 4));
        vo.setIdCardMasked(safeDecryptAndMask(pageDTO.getIdCard(), 3, 4));
        vo.setGender(pageDTO.getGender());
        vo.setOwnerSource(pageDTO.getOwnerSource());
        vo.setOwnerStatus(pageDTO.getOwnerStatus());
        vo.setCreateTime(pageDTO.getCreateTime());
        return vo;
    }

    /**
     * 安全解密并脱敏。解密失败时返回 "***"。
     */
    private String safeDecryptAndMask(String encrypted, int prefixLen, int suffixLen) {
        if (encrypted == null || encrypted.isEmpty()) {
            return "***";
        }
        try {
            String decrypted = encryptUtils.decrypt(encrypted);
            return EncryptUtils.mask(decrypted, prefixLen, suffixLen);
        } catch (Exception e) {
            log.warn("解密脱敏失败，返回默认值: {}", e.getMessage());
            return "***";
        }
    }

    /**
     * 将 OwnerDO 转换为 OwnerVO（含脱敏）。
     */
    private OwnerVO convertToOwnerVO(OwnerDO ownerDO) {
        OwnerVO vo = new OwnerVO();
        vo.setId(ownerDO.getId());
        vo.setUserId(ownerDO.getUserId());
        vo.setOwnerName(ownerDO.getOwnerName());
        // 明文字段（供编辑回填）
        vo.setName(ownerDO.getOwnerName());
        String decryptedPhone = encryptUtils.decrypt(ownerDO.getPhone());
        String decryptedIdCard = encryptUtils.decrypt(ownerDO.getIdCard());
        vo.setPhone(decryptedPhone);
        vo.setIdCard(decryptedIdCard);
        // 脱敏字段（供展示）
        vo.setPhoneMasked(EncryptUtils.mask(decryptedPhone, 3, 4));
        vo.setIdCardMasked(EncryptUtils.mask(decryptedIdCard, 3, 4));
        vo.setGender(ownerDO.getGender());
        vo.setAvatar(ownerDO.getAvatar());
        vo.setEmail(ownerDO.getEmail());
        vo.setEmergencyContact(ownerDO.getEmergencyContact());
        if (StringUtils.hasText(ownerDO.getEmergencyPhone())) {
            String decryptedEmergencyPhone = encryptUtils.decrypt(ownerDO.getEmergencyPhone());
            vo.setEmergencyPhone(decryptedEmergencyPhone);
            vo.setEmergencyPhoneMasked(EncryptUtils.mask(decryptedEmergencyPhone, 3, 4));
        }
        vo.setOwnerSource(ownerDO.getOwnerSource());
        vo.setOwnerStatus(ownerDO.getOwnerStatus());
        vo.setCreateTime(ownerDO.getCreateTime());
        vo.setUpdateTime(ownerDO.getUpdateTime());
        return vo;
    }
}
