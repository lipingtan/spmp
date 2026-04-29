package com.spmp.common.sequence.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 序列号段分配实体。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@TableName("cm_sequence_generator")
public class SequenceGeneratorDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String bizCode;

    private String dateKey;

    private Long maxAllocated;

    private Integer step;

    private Integer version;

    private Date createTime;

    private Date updateTime;
}
