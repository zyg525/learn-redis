package com.zyg.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zyg.entity.Shop;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.zyg.utils.RedisConstants.*;

/**
 * @Author: zyg
 * @Date: 2023/5/22 8:46
 * @Version: v1.0
 * @Description: 缓存工具类
 */
@Component
public class CacheClient {
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /**
     * 重建缓存的线程池
     */
    private static final ExecutorService CACHE_BUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    /**
     * 向Redis中插入缓存，并设置物理过期时间
     * @param key
     * @param value
     * @param time
     * @param timeUnit
     */
    public void set(String key, Object value, Long time, TimeUnit timeUnit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, timeUnit);
    }

    /**
     * 向Redis中插入缓存，并设置逻辑过期时间
     * @param key
     * @param value
     * @param time
     * @param timeUnit
     */
    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit timeUnit) {
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(timeUnit.toSeconds(time)));
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    /**
     * 获取Redis缓存，通过缓存空值解决缓存穿透问题
     * @param keyPrefix
     * @param id
     * @param type
     * @param time
     * @param timeUnit
     * @param dbFallback
     * @param <R>
     * @param <ID>
     * @return
     */
    public <R, ID> R queryWithPassThrough(String keyPrefix, ID id, Class<R> type, Long time, TimeUnit timeUnit, Function<ID, R> dbFallback) {
        //1、从Redis中查询商铺缓存
        String key = keyPrefix+id;
        String jsonStr = stringRedisTemplate.opsForValue().get(key);

        //2、判断缓存是否存在
        if(StrUtil.isNotBlank(jsonStr)) {
            //3、存在，返回缓存商铺信息
            R r = JSONUtil.toBean(jsonStr, type);
            return r;
        }

        //4、如果缓存的是空值，返回null(解决缓存穿透)
        if(jsonStr != null) {
            return null;
        }

        //5、不存在，从后端数据库中查询，重建缓存
        R r = dbFallback.apply(id);

        //6、后端数据库不存在，将空字符串写入Redis，返回null(解决缓存穿透)
        if(r == null) {
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }

        //7、后端数据库存在，写入Redis，重建缓存
        this.set(key, JSONUtil.toJsonStr(r), time, timeUnit);

        return r;
    }

    /**
     * 获取Redis缓存，通过使用逻辑过期时间解决缓存击穿问题
     * @param keyPrefix
     * @param id
     * @param type
     * @param time
     * @param timeUnit
     * @param dbFallback
     * @param <R>
     * @param <ID>
     * @return
     */
    public <R, ID> R queryWithLogicalExpire(String keyPrefix, ID id, Class<R> type, Long time, TimeUnit timeUnit, Function<ID, R> dbFallback) {
        //1、从Redis中查询商铺缓存
        String key = keyPrefix+id;
        String jsonStr = stringRedisTemplate.opsForValue().get(key);

        //2、判断缓存是否存在
        if(StrUtil.isBlank(jsonStr)) {
            //3、不存在，返回null
            return null;
        }

        //4、将缓存JSON转换成对象
        RedisData redisData = JSONUtil.toBean(jsonStr, RedisData.class);
        LocalDateTime expireTime = redisData.getExpireTime();
        R oldR = JSONUtil.toBean((JSONObject) redisData.getData(), type);

        //5、判断是否过期
        if(expireTime.isAfter(LocalDateTime.now())) {
            //6、未过期，返回结果
            return oldR;
        }

        //7、已过期，重建缓存
        String lockKey = LOCK_SHOP_KEY_PREFIX + id;
        //8、尝试获取锁
        boolean isLock = tryLock(lockKey);
        //9、获取到锁后，新开独立线程，重建缓存；未获取到锁则跳过
        if(isLock) {
            CACHE_BUILD_EXECUTOR.submit(() -> {
                try {
                    R newR = dbFallback.apply(id);
                    this.setWithLogicalExpire(key, newR, time, timeUnit);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    unLock(lockKey);
                }
            });
        }

        return oldR;
    }

    /**
     * 加锁
     * @param key
     * @return
     */
    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10L, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    /**
     * 解锁
     * @param key
     */
    private void unLock(String key) {
        stringRedisTemplate.delete(key);
    }
}
