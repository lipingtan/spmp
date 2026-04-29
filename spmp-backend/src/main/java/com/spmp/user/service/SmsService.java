package com.spmp.user.service;

/**
 * 短信服务接口（预留）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface SmsService {

    /**
     * 发送验证码短信。
     *
     * @param phone 手机号
     * @param code  验证码
     */
    void sendVerificationCode(String phone, String code);
}
