package com.zyg.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zyg.dao.ShopMapper;
import com.zyg.dto.Result;
import com.zyg.entity.Shop;
import com.zyg.service.ShopService;
import com.zyg.utils.RedisConstants;
import com.zyg.utils.RedisData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.zyg.utils.RedisConstants.*;

/**
 * @Author: zyg
 * @Date: 2023/5/21 14:18
 * @Version: v1.0
 * @Description:
 */
@Service
public class ShopServiceImpl implements ShopService {
    @Autowired
    ShopMapper shopMapper;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /**
     * 通过缓存NULL值解决缓存穿透
     * @param id
     * @return
     */
    @Override
    public Result queryShopById(Long id) {
        //1、使用互斥锁解决缓存击穿
        //Shop shop = queryWithMutex(id);

        //2、使用逻辑过期解决缓存击穿
        Shop shop = queryWithLogicalExpire(id);

        if(shop == null) {
            return Result.fail("店铺不存在");
        }

        return Result.ok(shop);
    }

    /**
     * 使用互斥锁解决缓存击穿
     * @param id
     * @return
     */
    @Override
    public Shop queryWithMutex(Long id) {
        //1、从Redis中查询商铺缓存
        String shopKey = CACHE_SHOP_KEY_PREFIX+id;
        String jsonStr = stringRedisTemplate.opsForValue().get(shopKey);

        //2、判断缓存是否存在
        if(StrUtil.isNotBlank(jsonStr)) {
            //3、存在，返回缓存商铺信息
            Shop shop = JSONUtil.toBean(jsonStr, Shop.class);
            return shop;
        }

        //4、如果缓存的是空值，返回null(解决缓存穿透)
        if(jsonStr != null) {
            return null;
        }

        //5、不存在，从后端数据库中查询，重建缓存
        Shop shop = null;
        String lockKey = LOCK_SHOP_KEY_PREFIX+id;
        try {
            //5.1、尝试获取互斥锁
            boolean isLock = tryLock(lockKey);
            if(!isLock) {
                //5.2、获取锁失败，休眠后重新查询
                Thread.sleep(50);
                return queryWithMutex(id);
            }

            //模拟重建缓存延时
            Thread.sleep(200);
            //5.2、获取锁成功，查询后端数据库
            shop = shopMapper.selectShopById(id);

            //6、后端数据库不存在，将空字符串写入Redis，返回null(解决缓存穿透)
            if(shop == null) {
                stringRedisTemplate.opsForValue().set(shopKey, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
                return null;
            }

            //7、后端数据库存在，写入Redis，重建缓存
            String shopStr = JSONUtil.toJsonStr(shop);
            stringRedisTemplate.opsForValue().set(shopKey, shopStr, CACHE_SHOP_TTL, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            //8、解锁
            unLock(lockKey);
        }
        //9、返回商铺信息
        return shop;
    }

    private static final ExecutorService CACHE_BUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    /**
     * 使用逻辑过期解决缓存击穿
     * @param id
     * @return
     */
    @Override
    public Shop queryWithLogicalExpire(Long id) {
        //1、从Redis中查询商铺缓存
        String shopKey = CACHE_SHOP_KEY_PREFIX+id;
        String jsonStr = stringRedisTemplate.opsForValue().get(shopKey);

        //2、判断缓存是否存在
        if(StrUtil.isBlank(jsonStr)) {
            //3、不存在，返回null
            return null;
        }

        //4、将缓存JSON转换成对象
        RedisData redisData = JSONUtil.toBean(jsonStr, RedisData.class);
        LocalDateTime expireTime = redisData.getExpireTime();
        Shop shop = JSONUtil.toBean((JSONObject) redisData.getData(), Shop.class);

        //5、判断是否过期
        if(expireTime.isAfter(LocalDateTime.now())) {
            //6、未过期，返回结果
            return shop;
        }

        //7、已过期，重建缓存
        String lockKey = LOCK_SHOP_KEY_PREFIX + id;
        //8、尝试获取锁
        boolean isLock = tryLock(lockKey);
        //9、获取到锁后，新开独立线程，重建缓存；未获取到锁则跳过
        if(isLock) {
            CACHE_BUILD_EXECUTOR.submit(() -> {
                try {
                    saveShop2Redis(id, 20L);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    unLock(lockKey);
                }
            });
        }

        return shop;
    }

    /**
     * 更新商铺
     * @param id
     * @param name
     * @return
     */
    @Override
    public Result updateShopNameById(Long id, String name) {
        //1、修改后端数据库
        shopMapper.updateShopById(id, name);

        //2、删除Redis缓存
        String shopKey = CACHE_SHOP_KEY_PREFIX+id;
        stringRedisTemplate.delete(shopKey);
        return Result.ok();
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

    /**
     * 将含有逻辑过期时间的数据写入Redis
     * @param id
     * @param expireSeconds
     */
    private void saveShop2Redis(Long id, Long expireSeconds) throws Exception {
        //模拟重建缓存延时
        Thread.sleep(1000);
        Shop shop = shopMapper.selectShopById(id);
        RedisData redisData = new RedisData();
        redisData.setData(shop);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY_PREFIX+id, JSONUtil.toJsonStr(redisData));
    }
}
