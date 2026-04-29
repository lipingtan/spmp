package com.spmp.common.sequence.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spmp.common.sequence.domain.entity.SequenceGeneratorDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 序列分配 Mapper。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Mapper
public interface SequenceGeneratorMapper extends BaseMapper<SequenceGeneratorDO> {

    @Select("SELECT id, biz_code, date_key, max_allocated, step, version, create_time, update_time " +
            "FROM cm_sequence_generator WHERE biz_code = #{bizCode} AND date_key = #{dateKey} LIMIT 1")
    SequenceGeneratorDO selectByBizAndDate(@Param("bizCode") String bizCode, @Param("dateKey") String dateKey);

    @Insert("INSERT INTO cm_sequence_generator (biz_code, date_key, max_allocated, step, version) " +
            "VALUES (#{bizCode}, #{dateKey}, 0, #{step}, 0) " +
            "ON DUPLICATE KEY UPDATE step = VALUES(step)")
    int initIfAbsent(@Param("bizCode") String bizCode, @Param("dateKey") String dateKey, @Param("step") int step);

    @Update("UPDATE cm_sequence_generator " +
            "SET max_allocated = max_allocated + #{step}, version = version + 1 " +
            "WHERE id = #{id} AND version = #{version}")
    int increaseByVersion(@Param("id") Long id, @Param("version") Integer version, @Param("step") int step);
}
