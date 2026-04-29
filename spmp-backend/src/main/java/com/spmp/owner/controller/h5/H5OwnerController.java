package com.spmp.owner.controller.h5;

import com.spmp.common.exception.BusinessException;
import com.spmp.common.result.Result;
import com.spmp.common.util.SecurityUtils;
import com.spmp.owner.domain.dto.H5RegisterDTO;
import com.spmp.owner.domain.vo.H5ProfileVO;
import com.spmp.owner.service.OwnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * H5 业主端 — 注册与个人信息 Controller。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/owner/h5")
@RequiredArgsConstructor
public class H5OwnerController {

    private final OwnerService ownerService;

    /**
     * 业主注册（允许匿名访问）。
     * 演示验证码：固定 123456 通过。
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody H5RegisterDTO registerDTO) {
        // 演示验证码校验（生产环境替换为短信验证码服务）
        if (!"123456".equals(registerDTO.getCaptcha())) {
            throw new BusinessException(4306, "验证码错误");
        }
        ownerService.register(registerDTO);
        return Result.success(null);
    }

    /**
     * 个人信息（需要 JWT 认证）。
     */
    @GetMapping("/profile")
    public Result<H5ProfileVO> getProfile() {
        Long ownerId = SecurityUtils.getCurrentOwnerId();
        return Result.success(ownerService.getProfile(ownerId));
    }
}
