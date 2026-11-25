package com.player.common.utils;

import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * token / 黑名单在 Redis 中的 key 约定（与 FastAPI 项目 common/utils/token_redis.py 完全一致，
 * 两个项目共用一个 Redis，token 可以跨项目互认）。
 * <p>
 * 为什么不用 token 原串当 key：JWT 普遍 200~400 字符，直接做 key 每个登录态要几百字节，登录态一多很吃内存。
 * 这里统一用 <b>md5(token) 的 32 位十六进制</b> 做 key（固定长度），Redis 里只存 userId、不存原始 token。
 * md5 只用于「键派生」，安全性不依赖抗碰撞：网关是先校验 JWT 签名再查 Redis 的。
 * <p>
 * key 约定：
 * <ul>
 *   <li>登录态：{@code login_token:{md5(token)}} -&gt; value = userId，TTL = token 有效期</li>
 *   <li>黑名单：{@code black_list_user:{userId}} -&gt; value 任意（存在即视为禁用）</li>
 * </ul>
 */
public final class TokenRedisKeys {

    /** 登录态 key 前缀 */
    public static final String TOKEN_KEY_PREFIX = "login_token:";

    /** 用户黑名单 key 前缀 */
    public static final String BLACKLIST_KEY_PREFIX = "black_list_user:";

    /** token 默认有效期（秒）：30 天，与 JwtToken 的 token 有效期一致 */
    public static final long DEFAULT_TOKEN_TTL_SECONDS = 30L * 24 * 3600;

    private TokenRedisKeys() {
    }

    /** token -> 32 位小写 md5 十六进制（与 FastAPI hashlib.md5(...).hexdigest() 结果一致） */
    public static String md5(String token) {
        return DigestUtils.md5DigestAsHex(token.getBytes(StandardCharsets.UTF_8));
    }

    /** 登录态 key：login_token:{md5(token)} */
    public static String tokenKey(String token) {
        return TOKEN_KEY_PREFIX + md5(token);
    }

    /** 黑名单 key：black_list_user:{userId} */
    public static String blacklistKey(String userId) {
        return BLACKLIST_KEY_PREFIX + userId;
    }
}
