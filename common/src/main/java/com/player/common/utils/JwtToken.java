package com.player.common.utils;

import com.alibaba.fastjson2.JSONObject;
import com.player.common.entity.UserEntity;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

public class JwtToken {

    private static final long DEFAULT_EXPIRATION_MS = 30 * 24 * 60 * 60 * 1000L; // 30天

    /**
     * 生成 JWT Token（线程安全）
     */
    public static String createToken(Object value, String secret) {
        SecretKey key = createHmacShaKey(secret);
        String jsonValue = JSONObject.toJSONString(value);

        return Jwts.builder()
                .issuedAt(Date.from(Instant.now()))
                .subject(jsonValue)
                .expiration(calculateExpiration(DEFAULT_EXPIRATION_MS))
                .signWith(key)
                .compact();
    }

    /**
     * 解析 Token（带完整异常处理）
     */
    public static <T> T parseToken(String token, Class<T> clazz, String secret) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return null;
            }
            String jwt = token.substring(7);

            return Jwts.parser()
                    .verifyWith(createHmacShaKey(secret))
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload()
                    .get("data", clazz);
        } catch (ExpiredJwtException e) {
            System.err.println("Token 已过期: " + e.getMessage());
        } catch (SecurityException | MalformedJwtException e) {
            System.err.println("Token 无效: " + e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("Token 解析失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 获取用户 ID
     */
    public static String getUserId(String token, String secret) {
        UserEntity user = parseToken(token, UserEntity.class, secret);
        return user != null ? user.getId() : null;
    }

    //-- 私有工具方法 --//
    private static SecretKey createHmacShaKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private static Date calculateExpiration(long expirationMs) {
        return Date.from(Instant.now().plusMillis(expirationMs));
    }
}