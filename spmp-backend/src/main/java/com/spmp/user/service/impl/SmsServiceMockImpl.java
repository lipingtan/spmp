package com.spmp.user.service.impl;

import com.spmp.user.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 短信服务模拟实现（开发环境使用）。
 * <p>
 * 仅日志输出验证码，不实际发送短信。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class SmsServiceMockImpl implements SmsService {

    @Override
    public void sendVerificationCode(String phone, String code) {
        log.info("【模拟短信】向手机号 {} 发送验证码: {}", phone, code);
    }
}
