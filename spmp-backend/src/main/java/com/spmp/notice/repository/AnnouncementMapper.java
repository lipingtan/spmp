package com.spmp.notice.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spmp.notice.domain.dto.NoticeQueryDTO;
import com.spmp.notice.domain.entity.AnnouncementDO;
import com.spmp.notice.domain.vo.NoticeListVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;

/**
 * 公告主表 Mapper。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Mapper
public interface AnnouncementMapper extends BaseMapper<AnnouncementDO> {

    /**
     * 管理端分页查询公告。
     *
     * @param page 分页参数
     * @param queryDTO 查询条件
     * @return 公告分页数据
     */
    IPage<NoticeListVO> selectManagePage(IPage<?> page, @Param("query") NoticeQueryDTO queryDTO);

    /**
     * 业主端分页查询我的公告。
     *
     * @param page 分页参数
     * @param userId 用户ID
     * @param noticeType 公告类型
     * @return 公告分页数据
     */
    IPage<NoticeListVO> selectMinePage(IPage<?> page,
                                       @Param("userId") Long userId,
                                       @Param("noticeType") String noticeType);

    /**
     * 统计指定时间后已发布公告数量（Dashboard 用）。
     *
     * @param publishSince 发布时间下限
     * @return 公告数
     */
    Long countPublishedSince(@Param("publishSince") LocalDateTime publishSince);
}
