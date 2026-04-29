package com.spmp.user.service;

import com.spmp.user.domain.dto.CaptchaDTO;
import com.spmp.user.domain.dto.LoginDTO;
import com.spmp.user.domain.dto.SmsLoginDTO;
import com.spmp.user.domain.dto.TokenDTO;

/**
 * 认证服务接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface AuthService {

    /**
     * 用户名密码登录。
     */
    TokenDTO login(LoginDTO loginDTO);

    /**
     * 手机号验证码登录。
     */
    TokenDTO loginBySms(SmsLoginDTO smsLoginDTO);

    /**
     * 发送短信验证码。
     */
    void sendSmsCode(String phone);

    /**
     * 刷新 Token。
     */
    TokenDTO refreshToken(String refreshToken);

    /**
     * 登出。
     */
    void logout(String accessToken);

    /**
     * 获取图形验证码。
     */
    CaptchaDTO getCaptcha();
}
