package com.spmp.notice.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spmp.notice.domain.entity.AnnouncementScopeDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公告范围 Mapper。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Mapper
public interface AnnouncementScopeMapper extends BaseMapper<AnnouncementScopeDO> {
}
