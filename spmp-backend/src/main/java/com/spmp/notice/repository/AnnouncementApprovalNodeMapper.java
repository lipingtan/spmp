package com.spmp.notice.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spmp.notice.domain.entity.AnnouncementApprovalNodeDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公告审批节点 Mapper。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Mapper
public interface AnnouncementApprovalNodeMapper extends BaseMapper<AnnouncementApprovalNodeDO> {
}
