package com.smartcanteen.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类：生成 Token、解析 Token、校验有效性。
 * 使用 HMAC-SHA 算法签名，Token 有效期为 24 小时。
 */
public class JwtUtil {

    /** 签名密钥，长度需满足 HMAC-SHA256 最低 256 bits */
    private static final String SECRET = "smart-canteen-jwt-secret-key-2024-minimum-256-bits!";
    /** Token 有效期：24 小时（毫秒） */
    private static final long EXPIRATION = 1000L * 60 * 60 * 24;
    /** RefreshToken 有效期：7 天（毫秒） */
    private static final long REFRESH_EXPIRATION = 1000L * 60 * 60 * 24 * 7;

    private JwtUtil() {
    }

    /** 构建 HMAC 签名密钥 */
    private static SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT Token（访问令牌），Payload 中包含 userId 和 role 两个声明
     *
     * @param userId 用户 ID
     * @param role   用户角色（STUDENT / MERCHANT / ADMIN）
     * @return JWT Token 字符串，有效期 24 小时
     */
    public static String generateToken(Long userId, String role) {
        return Jwts.builder()
                .claims(Map.of("userId", userId, "role", role, "type", "access"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getKey())
                .compact();
    }

    /**
     * 生成 RefreshToken（刷新令牌），有效期 7 天，仅用于换取新的访问令牌
     *
     * @param userId 用户 ID
     * @param role   用户角色
     * @return RefreshToken 字符串
     */
    public static String generateRefreshToken(Long userId, String role) {
        return Jwts.builder()
                .claims(Map.of("userId", userId, "role", role, "type", "refresh"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION))
                .signWith(getKey())
                .compact();
    }

    /**
     * 校验 RefreshToken 是否有效：签名正确、未过期、且 type 为 refresh
     */
    public static boolean isRefreshTokenValid(String token) {
        try {
            Claims claims = parseToken(token);
            if (!"refresh".equals(claims.get("type", String.class))) {
                return false;
            }
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 解析 Token，返回 Claims 对象。
     * 若 Token 签名无效、格式错误或已过期，会抛出异常。
     *
     * @param token JWT Token 字符串
     * @return 解析后的 Claims
     */
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 Token 中提取用户 ID
     *
     * @param token JWT Token 字符串
     * @return 用户 ID
     */
    public static Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从 Token 中提取用户角色
     *
     * @param token JWT Token 字符串
     * @return 角色字符串（STUDENT / MERCHANT / ADMIN）
     */
    public static String getRole(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }

    /**
     * 校验 AccessToken 是否有效：签名正确、未过期、且 type 为 access
     */
    public static boolean isAccessTokenValid(String token) {
        try {
            Claims claims = parseToken(token);
            if (!"access".equals(claims.get("type", String.class))) {
                return false;
            }
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断 Token 是否有效：签名正确且未过期
     *
     * @param token JWT Token 字符串
     * @return true 表示有效，false 表示无效或已过期
     */
    public static boolean isTokenValid(String token) {
        try {
            Claims claims = parseToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断 Token 是否已过期（isTokenValid 的反逻辑）
     *
     * @param token JWT Token 字符串
     * @return true 表示已过期或无效
     */
    public static boolean isTokenExpired(String token) {
        return !isTokenValid(token);
    }
}
