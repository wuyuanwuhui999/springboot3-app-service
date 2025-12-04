package com.player.common.utils;

import com.alibaba.fastjson2.JSONObject;
import io.jsonwebtoken.*;
import java.util.Base64;
import java.util.Date;
import com.player.common.entity.UserEntity;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class JwtToken {

    private static final long EXPIRATION_TIME = 2592000000L; // 30天

    /**
     * 生成JWT Token
     * @param value 载荷对象
     * @param secret 密钥
     * @return 带Bearer前缀的Token字符串
     */
    public static String createToken(Object value, String secret) {
        String jsonPayload = JSONObject.toJSONString(value);
        SignatureAlgorithm algorithm = SignatureAlgorithm.HS256;

        // 使用新的密钥生成方式
        SecretKey secretKey = generateSecretKey(secret);

        return Jwts.builder()
                .setIssuedAt(new Date())
                .setSubject(jsonPayload)
                .signWith(secretKey, algorithm)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .compact();
    }

    private static SecretKey generateSecretKey(String secret) {
        // 使用Base64解码替代已废弃的DatatypeConverter
        byte[] decodedKey = Base64.getDecoder().decode(secret);
        return new SecretKeySpec(decodedKey, SignatureAlgorithm.HS256.getJcaName());
    }

    /**
     * 解析JWT Token
     * @param token Token字符串
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 解析后的对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T parseToken(String token, Class<T> clazz, String secret) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(generateSecretKey(secret))
                    .build()
                    .parseClaimsJws(token.replace("Bearer ", ""))
                    .getBody();

            String payload = claims.getSubject();
            return (T) JSONObject.parseObject(payload, clazz);
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token已过期", e);
        } catch (JwtException e) {
            throw new RuntimeException("Token解析失败", e);
        }
    }

    public static String  getId(String token, String secret) {
        UserEntity user = parseToken(token, UserEntity.class, secret);
        return user != null ? user.getId() : null;
    }
}