package com.spmp.workorder.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 催单记录表实体。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@TableName("wo_urge_record")
public class UrgeRecordDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long urgeUserId;

    private String urgeUserName;

    private LocalDateTime urgeTime;

    private LocalDateTime createTime;
}
