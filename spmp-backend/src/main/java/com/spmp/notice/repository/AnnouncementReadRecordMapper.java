package com.spmp.notice.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spmp.notice.domain.entity.AnnouncementReadRecordDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公告已读记录 Mapper。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Mapper
public interface AnnouncementReadRecordMapper extends BaseMapper<AnnouncementReadRecordDO> {

    /**
     * 幂等写入已读记录。
     *
     * @param announcementId 公告ID
     * @param userId 用户ID
     * @return 影响行数
     */
    @Insert("INSERT IGNORE INTO nt_announcement_read_record(announcement_id, user_id, read_time) VALUES(#{announcementId}, #{userId}, NOW())")
    int insertIgnore(@Param("announcementId") Long announcementId, @Param("userId") Long userId);

    /**
     * 统计已读人数。
     *
     * @param announcementId 公告ID
     * @return 已读人数
     */
    @Select("SELECT COUNT(*) FROM nt_announcement_read_record WHERE announcement_id = #{announcementId}")
    long countReadByAnnouncement(@Param("announcementId") Long announcementId);
}
