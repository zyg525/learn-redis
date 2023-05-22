package com.zyg.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * @Author: zyg
 * @Date: 2023/5/22 9:46
 * @Version: v1.0
 * @Description: Redis唯一ID工具类
 */
@Component
public class RedisWorker {
    private static final Long BEGIN_TIMESTAMP = 1684664635000L;

    private static final Integer COUNT_BITS = 32;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /**
     * 生成唯一ID
     * @param keyPrefix
     * @return
     */
    public Long nextId(String keyPrefix) {
        //1、生成时间戳
        LocalDateTime now = LocalDateTime.now();
        long timestamp = now.toEpochSecond(ZoneOffset.UTC) - BEGIN_TIMESTAMP;

        //2、生成序列号
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        Long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);

        //3、拼接并返回
        return timestamp<<COUNT_BITS | count;
    }
}
