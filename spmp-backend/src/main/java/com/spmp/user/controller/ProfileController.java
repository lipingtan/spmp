package com.spmp.user.controller;

import com.spmp.common.result.Result;
import com.spmp.user.annotation.OperationLog;
import com.spmp.user.domain.dto.PasswordUpdateDTO;
import com.spmp.user.domain.dto.ProfileDTO;
import com.spmp.user.domain.dto.ProfileUpdateDTO;
import com.spmp.user.service.ProfileService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 个人中心控制器。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/user/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public Result<ProfileDTO> getProfile() {
        return Result.success(profileService.getProfile());
    }

    @PutMapping
    @OperationLog(module = "个人中心", type = "UPDATE", description = "修改个人信息")
    public Result<Void> updateProfile(@Valid @RequestBody ProfileUpdateDTO updateDTO) {
        profileService.updateProfile(updateDTO);
        return Result.success(null);
    }

    @PutMapping("/password")
    @OperationLog(module = "个人中心", type = "UPDATE", description = "修改密码")
    public Result<Void> updatePassword(@Valid @RequestBody PasswordUpdateDTO passwordDTO) {
        profileService.updatePassword(passwordDTO);
        return Result.success(null);
    }
}
