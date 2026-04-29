package com.spmp.owner.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spmp.common.exception.BusinessException;
import com.spmp.common.result.PageResult;
import com.spmp.common.util.SecurityUtils;
import com.spmp.owner.constant.CertStatus;
import com.spmp.owner.constant.OwnerConstants;
import com.spmp.owner.constant.OwnerErrorCode;
import com.spmp.owner.constant.OwnerStatus;
import com.spmp.owner.constant.RelationType;
import com.spmp.owner.domain.dto.CertificationApproveDTO;
import com.spmp.owner.domain.dto.CertificationBatchApproveDTO;
import com.spmp.owner.domain.dto.CertificationQueryDTO;
import com.spmp.owner.domain.dto.H5CertificationCreateDTO;
import com.spmp.owner.domain.dto.PropertyBindingCreateDTO;
import com.spmp.owner.domain.entity.CertificationDO;
import com.spmp.owner.domain.entity.OwnerDO;
import com.spmp.owner.domain.vo.CertificationVO;
import com.spmp.owner.repository.CertificationMapper;
import com.spmp.owner.repository.OwnerMapper;
import com.spmp.owner.service.CertificationService;
import com.spmp.owner.service.PropertyBindingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * 认证服务实现类。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class CertificationServiceImpl implements CertificationService {

    private final CertificationMapper certificationMapper;
    private final OwnerMapper ownerMapper;
    private final PropertyBindingService propertyBindingService;

    public CertificationServiceImpl(CertificationMapper certificationMapper,
                                    OwnerMapper ownerMapper,
                                    @Lazy PropertyBindingService propertyBindingService) {
        this.certificationMapper = certificationMapper;
        this.ownerMapper = ownerMapper;
        this.propertyBindingService = propertyBindingService;
    }

    @Override
    public PageResult<CertificationVO> listCertifications(CertificationQueryDTO queryDTO) {
        IPage<CertificationVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<CertificationVO> result = certificationMapper.selectCertificationPage(page, queryDTO);
        return PageResult.of(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveCertification(Long id, CertificationApproveDTO approveDTO) {
        CertificationDO certDO = certificationMapper.selectById(id);
        if (certDO == null) {
            throw new BusinessException(OwnerErrorCode.CERTIFICATION_NOT_FOUND.getCode(),
                    OwnerErrorCode.CERTIFICATION_NOT_FOUND.getMessage());
        }

        // 校验是否已处理
        if (!CertStatus.PENDING.getCode().equals(certDO.getCertStatus())) {
            throw new BusinessException(OwnerErrorCode.CERTIFICATION_ALREADY_PROCESSED.getCode(),
                    OwnerErrorCode.CERTIFICATION_ALREADY_PROCESSED.getMessage());
        }

        String action = approveDTO.getAction();
        if ("APPROVE".equals(action)) {
            handleApprove(certDO);
        } else if ("REJECT".equals(action)) {
            handleReject(certDO, approveDTO.getRejectReason());
        } else {
            throw new BusinessException(OwnerErrorCode.CERTIFICATION_NOT_FOUND.getCode(), "无效的审批操作: " + action);
        }

        log.info("认证审批完成，certId={}, action={}", id, action);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchApprove(CertificationBatchApproveDTO batchDTO) {
        List<Long> ids = batchDTO.getIds();

        // 校验数量不超过上限
        if (ids.size() > OwnerConstants.BATCH_APPROVE_LIMIT) {
            throw new BusinessException(OwnerErrorCode.CERTIFICATION_BATCH_LIMIT_EXCEEDED.getCode(),
                    OwnerErrorCode.CERTIFICATION_BATCH_LIMIT_EXCEEDED.getMessage());
        }

        // 逐条审批
        CertificationApproveDTO approveDTO = new CertificationApproveDTO();
        approveDTO.setAction(batchDTO.getAction());
        approveDTO.setRejectReason(batchDTO.getRejectReason());

        for (Long id : ids) {
            try {
                approveCertification(id, approveDTO);
            } catch (BusinessException e) {
                log.warn("批量审批中跳过已处理的认证申请，certId={}: {}", id, e.getMessage());
            }
        }

        log.info("批量审批完成，总数={}, action={}", ids.size(), batchDTO.getAction());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitCertification(Long ownerId, H5CertificationCreateDTO createDTO) {
        // 校验该房屋是否已有 PENDING 状态的申请（避免重复）
        LambdaQueryWrapper<CertificationDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CertificationDO::getOwnerId, ownerId)
                .eq(CertificationDO::getHouseId, createDTO.getHouseId())
                .eq(CertificationDO::getCertStatus, CertStatus.PENDING.getCode());
        Long pendingCount = certificationMapper.selectCount(wrapper);
        if (pendingCount > 0) {
            throw new BusinessException(OwnerErrorCode.CERTIFICATION_DUPLICATE_PENDING.getCode(),
                    OwnerErrorCode.CERTIFICATION_DUPLICATE_PENDING.getMessage());
        }

        // 创建认证申请
        CertificationDO certDO = new CertificationDO();
        certDO.setOwnerId(ownerId);
        certDO.setHouseId(createDTO.getHouseId());
        certDO.setCertStatus(CertStatus.PENDING.getCode());
        certDO.setApplyTime(new Date());
        certificationMapper.insert(certDO);

        // 更新业主状态为 CERTIFYING（如果当前是 UNCERTIFIED）
        OwnerDO ownerDO = ownerMapper.selectById(ownerId);
        if (ownerDO != null && OwnerStatus.UNCERTIFIED.getCode().equals(ownerDO.getOwnerStatus())) {
            ownerDO.setOwnerStatus(OwnerStatus.CERTIFYING.getCode());
            ownerMapper.updateById(ownerDO);
        }

        log.info("提交认证申请成功，certId={}, ownerId={}, houseId={}",
                certDO.getId(), ownerId, createDTO.getHouseId());
    }

    @Override
    public List<CertificationVO> listByOwnerId(Long ownerId) {
        // 使用 JOIN 查询直接获取含房屋地址信息的认证记录，避免 N+1 查询
        return certificationMapper.selectByOwnerId(ownerId);
    }

    @Override
    public List<CertificationDO> listTimeoutPending(int days) {
        return certificationMapper.selectTimeoutPending(days);
    }

    // ========== 私有方法 ==========

    /**
     * 处理审批通过逻辑。
     */
    private void handleApprove(CertificationDO certDO) {
        certDO.setCertStatus(CertStatus.APPROVED.getCode());
        certDO.setApproveTime(new Date());
        certDO.setApproverId(SecurityUtils.getCurrentUserId());
        certificationMapper.updateById(certDO);

        // 自动创建 relation_type=OWNER 的房产绑定
        PropertyBindingCreateDTO bindingDTO = new PropertyBindingCreateDTO();
        bindingDTO.setOwnerId(certDO.getOwnerId());
        bindingDTO.setHouseId(certDO.getHouseId());
        bindingDTO.setRelationType(RelationType.OWNER.getCode());
        propertyBindingService.bindProperty(bindingDTO);

        // 首次认证通过时更新业主状态为 CERTIFIED
        OwnerDO ownerDO = ownerMapper.selectById(certDO.getOwnerId());
        if (ownerDO != null) {
            String currentStatus = ownerDO.getOwnerStatus();
            if (OwnerStatus.UNCERTIFIED.getCode().equals(currentStatus)
                    || OwnerStatus.CERTIFYING.getCode().equals(currentStatus)) {
                ownerDO.setOwnerStatus(OwnerStatus.CERTIFIED.getCode());
                ownerMapper.updateById(ownerDO);
                log.info("业主首次认证通过，状态更新为 CERTIFIED，ownerId={}", ownerDO.getId());
            }
        }
    }

    /**
     * 处理审批驳回逻辑。
     */
    private void handleReject(CertificationDO certDO, String rejectReason) {
        // 校验驳回原因不为空
        if (!StringUtils.hasText(rejectReason)) {
            throw new BusinessException(OwnerErrorCode.CERTIFICATION_REJECT_REASON_REQUIRED.getCode(),
                    OwnerErrorCode.CERTIFICATION_REJECT_REASON_REQUIRED.getMessage());
        }

        certDO.setCertStatus(CertStatus.REJECTED.getCode());
        certDO.setApproveTime(new Date());
        certDO.setRejectReason(rejectReason);
        certDO.setApproverId(SecurityUtils.getCurrentUserId());
        certificationMapper.updateById(certDO);
    }
}
