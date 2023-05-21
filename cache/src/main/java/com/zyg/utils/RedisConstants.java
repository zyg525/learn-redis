package com.zyg.utils;

/**
 * @Author: zyg
 * @Date: 2023/5/20 18:34
 * @Version: v1.0
 * @Description: Redis常量类
 */
public class RedisConstants {
    /**
     * 登录验证码前缀
     */
    public static final String LOGIN_CODE_KEY_PREFIX = "login:code:";

    /**
     * 登录令牌前缀
     */
    public static final String LOGIN_TOKEN_KEY_PREFIX = "login:token:";

    /**
     * 商铺缓存前缀
     */
    public static final String CACHE_SHOP_KEY_PREFIX = "cache:shop:";

    /**
     * 商铺互斥锁缓存前缀
     */
    public static final String LOCK_SHOP_KEY_PREFIX = "lock:shop:";

    /**
     * 商铺缓存过期时间(分钟)
     */
    public static final Long CACHE_SHOP_TTL = 30L;

    /**
     * 空值缓存过期时间(分钟)
     */
    public static final Long CACHE_NULL_TTL = 10L;

    /**
     * 登录验证码过期时间(分钟)
     */
    public static final Long LOGIN_CODE_TTL = 20L;

    /**
     * 登录令牌过期时间(分钟)
     */
    public static final Long LOGIN_TOKEN_TTL = 30L;
}
