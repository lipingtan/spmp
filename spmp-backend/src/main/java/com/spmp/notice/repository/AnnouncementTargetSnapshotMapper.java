package com.spmp.notice.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.spmp.notice.domain.entity.AnnouncementTargetSnapshotDO;
import com.spmp.notice.domain.vo.NoticeUnreadUserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 公告目标快照 Mapper。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Mapper
public interface AnnouncementTargetSnapshotMapper extends BaseMapper<AnnouncementTargetSnapshotDO> {

    /**
     * 统计快照目标人数。
     *
     * @param announcementId 公告ID
     * @return 目标人数
     */
    @Select("SELECT COUNT(*) FROM nt_announcement_target_snapshot WHERE announcement_id = #{announcementId}")
    long countTargetByAnnouncement(@Param("announcementId") Long announcementId);

    /**
     * 判断用户是否在公告快照范围内。
     *
     * @param announcementId 公告ID
     * @param userId 用户ID
     * @return 匹配数量
     */
    @Select("SELECT COUNT(*) FROM nt_announcement_target_snapshot WHERE announcement_id = #{announcementId} AND user_id = #{userId}")
    long countTargetByAnnouncementAndUser(@Param("announcementId") Long announcementId, @Param("userId") Long userId);

    /**
     * 分页查询未读用户。
     *
     * @param page 分页参数
     * @param announcementId 公告ID
     * @return 未读用户分页
     */
    IPage<NoticeUnreadUserVO> selectUnreadUsersPage(IPage<?> page, @Param("announcementId") Long announcementId);

    /**
     * 按范围批量生成目标快照（ALL/COMMUNITY/BUILDING）。
     *
     * @param announcementId 公告ID
     * @param scopeType 范围类型：ALL/COMMUNITY/BUILDING
     * @param targetId 目标ID（ALL 传 null）
     * @return 插入行数
     */
    int insertSnapshotByScope(@Param("announcementId") Long announcementId,
                              @Param("scopeType") String scopeType,
                              @Param("targetId") Long targetId);
}
