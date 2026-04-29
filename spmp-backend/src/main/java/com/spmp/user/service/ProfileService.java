package com.spmp.user.service;

import com.spmp.user.domain.dto.PasswordUpdateDTO;
import com.spmp.user.domain.dto.ProfileDTO;
import com.spmp.user.domain.dto.ProfileUpdateDTO;

/**
 * 个人中心服务接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface ProfileService {

    ProfileDTO getProfile();

    void updateProfile(ProfileUpdateDTO updateDTO);

    void updatePassword(PasswordUpdateDTO passwordDTO);
}
