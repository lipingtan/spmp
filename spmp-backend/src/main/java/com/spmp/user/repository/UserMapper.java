package com.spmp.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spmp.user.domain.entity.UserDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户 Mapper 接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Mapper
public interface UserMapper extends BaseMapper<UserDO> {

    /**
     * 根据用户名查询用户。
     *
     * @param username 用户名
     * @return 用户实体
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND del_flag = 0")
    UserDO selectByUsername(@Param("username") String username);

    /**
     * 根据手机号哈希查询用户。
     *
     * @param phoneHash 手机号 SHA-256 哈希
     * @return 用户实体
     */
    @Select("SELECT * FROM sys_user WHERE phone_hash = #{phoneHash} AND del_flag = 0")
    UserDO selectByPhoneHash(@Param("phoneHash") String phoneHash);
}
