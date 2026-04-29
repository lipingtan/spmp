package com.spmp.owner.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.spmp.common.security.DataPermission;
import com.spmp.owner.domain.dto.CertificationQueryDTO;
import com.spmp.owner.domain.entity.CertificationDO;
import com.spmp.owner.domain.vo.CertificationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 认证申请 Mapper 接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Mapper
public interface CertificationMapper extends BaseMapper<CertificationDO> {

    /**
     * 分页查询认证申请（数据权限过滤）。
     * <p>
     * JOIN bs_house 实现数据权限过滤。
     *
     * @param page     分页参数
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @DataPermission(buildingField = "bd.id")
    IPage<CertificationVO> selectCertificationPage(IPage<?> page, @Param("query") CertificationQueryDTO queryDTO);

    /**
     * 查询超时未审批的认证申请。
     *
     * @param days 超时天数
     * @return 超时认证申请列表
     */
    List<CertificationDO> selectTimeoutPending(@Param("days") int days);

    /**
     * 按业主ID查询认证记录（含房屋地址信息，供 H5 端使用）。
     *
     * @param ownerId 业主ID
     * @return 认证记录列表（含小区/楼栋/单元/房屋名称）
     */
    List<CertificationVO> selectByOwnerId(@Param("ownerId") Long ownerId);
}
