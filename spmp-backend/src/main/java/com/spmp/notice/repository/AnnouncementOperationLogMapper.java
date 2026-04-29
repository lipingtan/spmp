package com.spmp.notice.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spmp.notice.domain.entity.AnnouncementOperationLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公告业务操作日志 Mapper。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Mapper
public interface AnnouncementOperationLogMapper extends BaseMapper<AnnouncementOperationLogDO> {
}
