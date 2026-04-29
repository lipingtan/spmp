package com.spmp.user.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.spmp.common.exception.BusinessException;
import com.spmp.common.exception.ErrorCode;
import com.spmp.common.security.JwtTokenProvider;
import com.spmp.common.util.EncryptUtils;
import com.spmp.common.util.RedisUtils;
import com.spmp.user.constant.UserConstants;
import com.spmp.user.constant.UserErrorCode;
import com.spmp.user.domain.dto.*;
import com.spmp.user.domain.entity.LoginLogDO;
import com.spmp.user.domain.entity.RoleDO;
import com.spmp.user.domain.entity.UserDO;
import com.spmp.user.repository.RoleMapper;
import com.spmp.user.repository.UserMapper;
import com.spmp.user.repository.UserRoleMapper;
import com.spmp.user.service.AuthService;
import com.spmp.user.service.LoginLogService;
import com.spmp.user.service.PermissionCacheService;
import com.spmp.user.service.SmsService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 认证服务实现。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RedisUtils redisUtils;
    private final EncryptUtils encryptUtils;
    private final SmsService smsService;
    private final PermissionCacheService permissionCacheService;
    private final LoginLogService loginLogService;
    private final JdbcTemplate jdbcTemplate;

    @org.springframework.beans.factory.annotation.Value("${captcha.skip:false}")
    private boolean captchaSkip;

    public AuthServiceImpl(UserMapper userMapper, UserRoleMapper userRoleMapper,
                           RoleMapper roleMapper, JwtTokenProvider jwtTokenProvider,
                           BCryptPasswordEncoder passwordEncoder, RedisUtils redisUtils,
                           EncryptUtils encryptUtils, SmsService smsService,
                           PermissionCacheService permissionCacheService,
                           LoginLogService loginLogService,
                           JdbcTemplate jdbcTemplate) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.redisUtils = redisUtils;
        this.encryptUtils = encryptUtils;
        this.smsService = smsService;
        this.permissionCacheService = permissionCacheService;
        this.loginLogService = loginLogService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public CaptchaDTO getCaptcha() {
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 5);
        String code = captcha.getCode();
        String key = UUID.randomUUID().toString().replace("-", "");

        // 存入 Redis
        redisUtils.set(UserConstants.CAPTCHA_KEY + key, code,
                UserConstants.CAPTCHA_EXPIRE_SECONDS, TimeUnit.SECONDS);

        CaptchaDTO dto = new CaptchaDTO();
        dto.setCaptchaKey(key);
        dto.setCaptchaImage("data:image/png;base64," +
                Base64.getEncoder().encodeToString(captcha.getImageBytes()));
        return dto;
    }

    @Override
    public TokenDTO login(LoginDTO loginDTO) {
        // 1. 校验图形验证码
        validateCaptcha(loginDTO.getCaptchaKey(), loginDTO.getCaptchaCode());

        String username = loginDTO.getUsername();

        // 2. 检查账号锁定
        checkAccountLocked(username);

        // 3. 查询用户
        UserDO user = userMapper.selectByUsername(username);
        if (user == null) {
            recordLoginFail(username);
            throw new BusinessException(ErrorCode.BAD_REQUEST, UserErrorCode.USER_NOT_FOUND.getMessage());
        }

        // 4. 校验密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            recordLoginFail(username);
            throw new BusinessException(ErrorCode.BAD_REQUEST, UserErrorCode.PASSWORD_ERROR.getMessage());
        }

        // 5. 检查用户状态
        if (UserConstants.STATUS_DISABLED == user.getStatus()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, UserErrorCode.ACCOUNT_DISABLED.getMessage());
        }

        // 6. 登录成功：清除错误计数
        clearLoginFail(username);

        // 7. 生成 Token
        return generateTokenDTO(user, loginDTO.getClientType());
    }

    @Override
    public TokenDTO loginBySms(SmsLoginDTO smsLoginDTO) {
        String phone = smsLoginDTO.getPhone();
        String phoneHash = DigestUtil.sha256Hex(phone);

        // 1. 校验短信验证码
        String codeKey = UserConstants.SMS_CODE_KEY + phoneHash;
        String cachedCode = redisUtils.get(codeKey, String.class);
        if (cachedCode == null || !cachedCode.equals(smsLoginDTO.getSmsCode())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, UserErrorCode.SMS_CODE_ERROR.getMessage());
        }
        // 验证成功后删除
        redisUtils.delete(codeKey);

        // 2. 查询用户
        UserDO user = userMapper.selectByPhoneHash(phoneHash);
        if (user == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, UserErrorCode.PHONE_NOT_REGISTERED.getMessage());
        }

        // 3. 检查用户状态
        if (UserConstants.STATUS_DISABLED == user.getStatus()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, UserErrorCode.ACCOUNT_DISABLED.getMessage());
        }

        // 4. 生成 Token
        return generateTokenDTO(user, smsLoginDTO.getClientType());
    }

    @Override
    public void sendSmsCode(String phone) {
        String phoneHash = DigestUtil.sha256Hex(phone);

        // 1. 校验 60 秒发送间隔
        String intervalKey = UserConstants.SMS_INTERVAL_KEY + phoneHash;
        if (Boolean.TRUE.equals(redisUtils.hasKey(intervalKey))) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, UserErrorCode.SMS_SEND_TOO_FREQUENT.getMessage());
        }

        // 2. 校验每日发送上限
        String dailyKey = UserConstants.SMS_DAILY_KEY + phoneHash;
        Long dailyCount = redisUtils.increment(dailyKey, 0);
        if (dailyCount != null && dailyCount >= UserConstants.SMS_DAILY_LIMIT) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, UserErrorCode.SMS_DAILY_LIMIT.getMessage());
        }

        // 3. 生成 6 位验证码
        String code = RandomUtil.randomNumbers(6);

        // 4. 存入 Redis
        redisUtils.set(UserConstants.SMS_CODE_KEY + phoneHash, code,
                UserConstants.SMS_CODE_EXPIRE_SECONDS, TimeUnit.SECONDS);

        // 5. 设置发送间隔
        redisUtils.set(intervalKey, "1", UserConstants.SMS_INTERVAL_SECONDS, TimeUnit.SECONDS);

        // 6. 递增每日计数
        redisUtils.increment(dailyKey, 1);
        redisUtils.expire(dailyKey, 24, TimeUnit.HOURS);

        // 7. 发送短信
        smsService.sendVerificationCode(phone, code);
    }

    @Override
    public TokenDTO refreshToken(String refreshToken) {
        // 1. 校验 refreshToken
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, UserErrorCode.TOKEN_INVALID.getMessage());
        }

        Claims claims = jwtTokenProvider.parseToken(refreshToken);

        // 2. 校验 tokenType
        String tokenType = claims.get("tokenType", String.class);
        if (!"refresh".equals(tokenType)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, UserErrorCode.TOKEN_INVALID.getMessage());
        }

        // 3. 校验黑名单
        String jti = claims.getId();
        if (jti != null && Boolean.TRUE.equals(redisUtils.hasKey(UserConstants.TOKEN_BLACKLIST_KEY + jti))) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, UserErrorCode.TOKEN_INVALID.getMessage());
        }

        // 4. 获取用户信息
        Long userId = ((Number) claims.get("userId")).longValue();
        String clientType = claims.get("clientType", String.class);

        UserDO user = userMapper.selectById(userId);
        if (user == null || UserConstants.STATUS_DISABLED == user.getStatus()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, UserErrorCode.TOKEN_INVALID.getMessage());
        }

        // 5. 将旧 refreshToken 加入黑名单
        if (jti != null) {
            long remainingMs = claims.getExpiration().getTime() - System.currentTimeMillis();
            if (remainingMs > 0) {
                redisUtils.set(UserConstants.TOKEN_BLACKLIST_KEY + jti, "1",
                        remainingMs, TimeUnit.MILLISECONDS);
            }
        }

        // 6. 生成新 Token
        return generateTokenDTO(user, clientType);
    }

    @Override
    public void logout(String accessToken) {
        try {
            Claims claims = jwtTokenProvider.parseToken(accessToken);
            String jti = claims.getId();

            // 将 accessToken 加入黑名单
            if (jti != null) {
                long remainingMs = claims.getExpiration().getTime() - System.currentTimeMillis();
                if (remainingMs > 0) {
                    redisUtils.set(UserConstants.TOKEN_BLACKLIST_KEY + jti, "1",
                            remainingMs, TimeUnit.MILLISECONDS);
                }
            }

            // 清除用户权限缓存
            Long userId = ((Number) claims.get("userId")).longValue();
            permissionCacheService.clearUserPermissions(userId);
            permissionCacheService.clearUserDataPermission(userId);
            permissionCacheService.clearUserMenus(userId);
        } catch (Exception e) {
            log.warn("登出处理异常: {}", e.getMessage());
        }
    }

    // ========== 私有方法 ==========

    /**
     * 校验图形验证码。
     */
    private void validateCaptcha(String captchaKey, String captchaCode) {
        // 测试模式：跳过验证码校验
        if (captchaSkip) {
            log.debug("测试模式：跳过验证码校验");
            return;
        }
        String key = UserConstants.CAPTCHA_KEY + captchaKey;
        String cachedCode = redisUtils.get(key, String.class);
        if (cachedCode == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, UserErrorCode.CAPTCHA_EXPIRED.getMessage());
        }
        if (!cachedCode.equalsIgnoreCase(captchaCode)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, UserErrorCode.CAPTCHA_ERROR.getMessage());
        }
        // 验证成功后删除
        redisUtils.delete(key);
    }

    /**
     * 检查账号是否被锁定。
     */
    private void checkAccountLocked(String username) {
        String lockKey = UserConstants.LOGIN_LOCK_KEY + username;
        if (Boolean.TRUE.equals(redisUtils.hasKey(lockKey))) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, UserErrorCode.ACCOUNT_LOCKED.getMessage());
        }
    }

    /**
     * 记录登录失败。
     */
    private void recordLoginFail(String username) {
        String failKey = UserConstants.LOGIN_FAIL_KEY + username;
        Long failCount = redisUtils.increment(failKey, 1);
        redisUtils.expire(failKey, UserConstants.LOCK_MINUTES, TimeUnit.MINUTES);

        if (failCount != null && failCount >= UserConstants.MAX_LOGIN_FAIL_COUNT) {
            // 锁定账号
            redisUtils.set(UserConstants.LOGIN_LOCK_KEY + username, "1",
                    UserConstants.LOCK_MINUTES, TimeUnit.MINUTES);
        }

        // 记录登录失败日志
        saveLoginLog(username, 1, "密码错误，第" + failCount + "次");
    }

    /**
     * 清除登录失败计数。
     */
    private void clearLoginFail(String username) {
        redisUtils.delete(UserConstants.LOGIN_FAIL_KEY + username);
        redisUtils.delete(UserConstants.LOGIN_LOCK_KEY + username);
    }

    /**
     * 生成 TokenDTO。
     */
    private TokenDTO generateTokenDTO(UserDO user, String clientType) {
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(user.getId());
        List<String> roleCodes = roleIds.stream()
                .map(roleId -> {
                    RoleDO role = roleMapper.selectById(roleId);
                    return role != null ? role.getRoleCode() : null;
                })
                .filter(code -> code != null)
                .collect(Collectors.toList());

        String accessToken = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), roleCodes, clientType, resolveOwnerId(user.getId(), roleCodes));
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), clientType);

        // 缓存权限
        permissionCacheService.getUserPermissions(user.getId());
        permissionCacheService.getDataPermission(user.getId());

        TokenDTO dto = new TokenDTO();
        dto.setAccessToken(accessToken);
        dto.setRefreshToken(refreshToken);
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setAvatar(user.getAvatar());

        // 异步记录登录成功日志
        saveLoginLog(user.getUsername(), 0, null);

        return dto;
    }

    /**
     * 异步记录登录日志。
     *
     * @param username    用户名
     * @param loginResult 登录结果（0-成功 1-失败）
     * @param failReason  失败原因
     */
    private void saveLoginLog(String username, int loginResult, String failReason) {
        try {
            LoginLogDO logDO = new LoginLogDO();
            logDO.setUsername(username);
            logDO.setLoginResult(loginResult);
            logDO.setFailReason(failReason);
            logDO.setLoginTime(new Date());

            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                logDO.setLoginIp(getClientIp(request));
                logDO.setBrowser(request.getHeader("User-Agent"));
            }

            loginLogService.saveLoginLog(logDO);
        } catch (Exception e) {
            log.warn("记录登录日志失败: {}", e.getMessage());
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private Long resolveOwnerId(Long userId, List<String> roleCodes) {
        if (!roleCodes.contains("owner")) {
            return null;
        }
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT id FROM ow_owner WHERE user_id = ? AND del_flag = 0 LIMIT 1",
                    Long.class, userId);
        } catch (Exception e) {
            log.warn("查询 ownerId 失败, userId={}: {}", userId, e.getMessage());
            return null;
        }
    }
}
