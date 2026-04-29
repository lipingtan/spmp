package com.spmp.owner.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spmp.owner.domain.entity.FamilyMemberDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 家庭成员 Mapper 接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Mapper
public interface FamilyMemberMapper extends BaseMapper<FamilyMemberDO> {
}
