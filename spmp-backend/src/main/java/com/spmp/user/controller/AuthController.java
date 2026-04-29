package com.spmp.user.controller;

import com.spmp.common.result.Result;
import com.spmp.user.annotation.RateLimit;
import com.spmp.user.domain.dto.CaptchaDTO;
import com.spmp.user.domain.dto.LoginDTO;
import com.spmp.user.domain.dto.SmsLoginDTO;
import com.spmp.user.domain.dto.TokenDTO;
import com.spmp.user.service.AuthService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 认证控制器。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/user/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/captcha")
    public Result<CaptchaDTO> getCaptcha() {
        return Result.success(authService.getCaptcha());
    }

    @PostMapping("/login")
    @RateLimit(key = "login", window = 60, maxCount = 10, dimension = "IP")
    public Result<TokenDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success(authService.login(loginDTO));
    }

    @PostMapping("/login/sms")
    @RateLimit(key = "sms-login", window = 60, maxCount = 10, dimension = "IP")
    public Result<TokenDTO> loginBySms(@Valid @RequestBody SmsLoginDTO smsLoginDTO) {
        return Result.success(authService.loginBySms(smsLoginDTO));
    }

    @PostMapping("/sms-code")
    @RateLimit(key = "sms-code", window = 60, maxCount = 1, dimension = "IP")
    public Result<Void> sendSmsCode(@RequestParam String phone) {
        authService.sendSmsCode(phone);
        return Result.success(null);
    }

    @PostMapping("/refresh")
    @RateLimit(key = "refresh", window = 60, maxCount = 20, dimension = "IP")
    public Result<TokenDTO> refreshToken(@RequestParam String refreshToken) {
        return Result.success(authService.refreshToken(refreshToken));
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        authService.logout(token);
        return Result.success(null);
    }
}
