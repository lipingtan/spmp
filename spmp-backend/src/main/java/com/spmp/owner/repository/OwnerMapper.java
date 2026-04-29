package com.spmp.owner.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.spmp.common.security.DataPermission;
import com.spmp.owner.domain.dto.OwnerPageDTO;
import com.spmp.owner.domain.dto.OwnerQueryDTO;
import com.spmp.owner.domain.entity.OwnerDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 业主 Mapper 接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Mapper
public interface OwnerMapper extends BaseMapper<OwnerDO> {

    /**
     * 分页查询业主列表（数据权限过滤）。
     * <p>
     * JOIN ow_property_binding + bs_house 实现数据权限过滤，
     * 支持姓名模糊搜索、手机号精确匹配（加密后）、状态筛选、小区/楼栋筛选。
     *
     * @param page     分页参数
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @DataPermission(buildingField = "bd.id")
    IPage<OwnerPageDTO> selectOwnerPage(IPage<?> page, @Param("query") OwnerQueryDTO queryDTO);

    /**
     * 按加密手机号查询业主（用于注册时匹配预录入）。
     *
     * @param encryptedPhone 加密后的手机号
     * @return 业主实体，不存在返回 null
     */
    OwnerDO selectByEncryptedPhone(@Param("encryptedPhone") String encryptedPhone);

    /**
     * 按加密身份证号查询业主（用于唯一性校验）。
     *
     * @param encryptedIdCard 加密后的身份证号
     * @return 业主列表
     */
    List<OwnerDO> selectByEncryptedIdCard(@Param("encryptedIdCard") String encryptedIdCard);
}
