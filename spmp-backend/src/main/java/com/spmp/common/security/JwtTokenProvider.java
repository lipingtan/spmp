package com.spmp.common.security;

import com.spmp.common.exception.BusinessException;
import com.spmp.common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * JWT 令牌管理组件。
 * <p>
 * 负责 JWT 令牌的生成、解析和验证，支持 admin/owner 两种过期策略。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:defaultSecretKeyForDevelopmentOnly1234}")
    private String secret;

    /** PC 端 Access Token 过期时间，默认 8 小时 */
    @Value("${jwt.admin-expiration:28800000}")
    private long adminExpiration;

    /** H5 端 Access Token 过期时间，默认 7 天 */
    @Value("${jwt.owner-expiration:604800000}")
    private long ownerExpiration;

    /**
     * 生成 Access Token。
     *
     * @param userId     用户 ID
     * @param username   用户名
     * @param roles      角色列表
     * @param clientType 客户端类型（admin/owner）
     * @return JWT 令牌字符串
     */
    public String generateToken(Long userId, String username, List<String> roles, String clientType) {
        return generateToken(userId, username, roles, clientType, null);
    }

    public String generateToken(Long userId, String username, List<String> roles, String clientType, Long ownerId) {
        long expiration = getExpiration(clientType);
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        JwtBuilder builder = Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(username)
                .claim("userId", userId)
                .claim("username", username)
                .claim("roles", roles)
                .claim("clientType", clientType);
        if (ownerId != null) {
            builder.claim("ownerId", ownerId);
        }
        return builder
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 生成 Refresh Token。
     * <p>
     * 过期时间为对应 Access Token 的 2 倍。
     *
     * @param userId     用户 ID
     * @param clientType 客户端类型（admin/owner）
     * @return Refresh Token 字符串
     */
    public String generateRefreshToken(Long userId, String clientType) {
        long expiration = getExpiration(clientType) * 2;
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .claim("userId", userId)
                .claim("clientType", clientType)
                .claim("tokenType", "refresh")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 JWT 令牌。
     *
     * @param token JWT 令牌字符串
     * @return Claims 信息
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 验证 JWT 令牌有效性。
     *
     * @param token JWT 令牌字符串
     * @return 验证通过返回 true
     * @throws BusinessException 签名无效或令牌过期时抛出
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "令牌签名无效");
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "令牌已过期");
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "令牌无效");
        }
    }

    /**
     * 根据客户端类型获取过期时间。
     */
    private long getExpiration(String clientType) {
        return "admin".equals(clientType) ? adminExpiration : ownerExpiration;
    }

    /**
     * 获取签名密钥。
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
