package com.spmp.owner.service;

import com.spmp.common.result.PageResult;
import com.spmp.owner.domain.dto.CertificationApproveDTO;
import com.spmp.owner.domain.dto.CertificationBatchApproveDTO;
import com.spmp.owner.domain.dto.CertificationQueryDTO;
import com.spmp.owner.domain.dto.H5CertificationCreateDTO;
import com.spmp.owner.domain.entity.CertificationDO;
import com.spmp.owner.domain.vo.CertificationVO;

import java.util.List;

/**
 * 认证服务接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface CertificationService {

    /**
     * 分页查询认证申请列表（数据权限过滤）。
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<CertificationVO> listCertifications(CertificationQueryDTO queryDTO);

    /**
     * 单条审批（通过时自动创建房产绑定，首次认证通过时更新业主状态为 CERTIFIED）。
     *
     * @param id         认证申请ID
     * @param approveDTO 审批参数
     */
    void approveCertification(Long id, CertificationApproveDTO approveDTO);

    /**
     * 批量审批（单次最多 100 条）。
     *
     * @param batchDTO 批量审批参数
     */
    void batchApprove(CertificationBatchApproveDTO batchDTO);

    /**
     * H5 端提交认证申请。
     *
     * @param ownerId   业主ID
     * @param createDTO 认证申请参数
     */
    void submitCertification(Long ownerId, H5CertificationCreateDTO createDTO);

    /**
     * 查询业主的认证记录。
     *
     * @param ownerId 业主ID
     * @return 认证记录列表
     */
    List<CertificationVO> listByOwnerId(Long ownerId);

    /**
     * 查询超时未审批的认证申请（供定时任务调用）。
     *
     * @param days 超时天数
     * @return 超时认证申请列表
     */
    List<CertificationDO> listTimeoutPending(int days);
}
