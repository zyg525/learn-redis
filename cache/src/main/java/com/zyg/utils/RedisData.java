package com.zyg.utils;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: zyg
 * @Date: 2023/5/21 17:42
 * @Version: v1.0
 * @Description:
 */
@Data
public class RedisData {
    /**
     * 逻辑过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 数据
     */
    private Object data;
}
