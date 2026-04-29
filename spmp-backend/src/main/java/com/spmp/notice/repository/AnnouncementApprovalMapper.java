package com.spmp.notice.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spmp.notice.domain.entity.AnnouncementApprovalDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公告审批实例 Mapper。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Mapper
public interface AnnouncementApprovalMapper extends BaseMapper<AnnouncementApprovalDO> {
}
