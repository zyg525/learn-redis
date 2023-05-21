package com.zyg.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.zyg.dto.UserDto;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.zyg.utils.RedisConstants.LOGIN_TOKEN_KEY_PREFIX;
import static com.zyg.utils.RedisConstants.LOGIN_TOKEN_TTL;

/**
 * @Author: zyg
 * @Date: 2023/5/21 11:36
 * @Version: v1.0
 * @Description: 刷新Token拦截器
 */
public class RefreshTokenInterceptor implements HandlerInterceptor {

    StringRedisTemplate stringRedisTemplate;

    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 针对所有请求，刷新token有效期
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1、获取请求头中的token
        String token = request.getHeader("authorization");
        if(StrUtil.isBlank(token)) {
            //2、token不存在，放行
            return true;
        }
        //3、根据token获取Redis中的用户
        String tokenKey = LOGIN_TOKEN_KEY_PREFIX+token;
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(tokenKey);

        //4、判断用户是否存在
        if(userMap.isEmpty()) {
            //5、用户不存在，放行
            return true;
        }

        //6、将查到的hash对象转换为UserDto对象
        UserDto userDto = BeanUtil.fillBeanWithMap(userMap, new UserDto(), false);

        //7、保存用户信息到ThreadLocal
        UserHolder.saveUserDto(userDto);

        //8、刷新token有效期
        stringRedisTemplate.expire(tokenKey, LOGIN_TOKEN_TTL, TimeUnit.MINUTES);

        //9、放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
