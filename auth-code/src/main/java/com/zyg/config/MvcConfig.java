package com.zyg.config;

import com.zyg.utils.LoginInterceptor;
import com.zyg.utils.RefreshTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author: zyg
 * @Date: 2023/5/21 10:36
 * @Version: v1.0
 * @Description: Mvc配置类
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate))
                .addPathPatterns("/**")
                .order(1);
        registry.addInterceptor(new LoginInterceptor(stringRedisTemplate))
                .addPathPatterns("/access")
                .order(2);
    }
}
