package com.zyg.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import com.zyg.dao.UserMapper;
import com.zyg.dto.LoginFormDto;
import com.zyg.dto.Result;
import com.zyg.dto.UserDto;
import com.zyg.entity.User;
import com.zyg.service.UserService;
import com.zyg.utils.RedisConstants;
import com.zyg.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.zyg.utils.RedisConstants.*;
import static com.zyg.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * @Author: zyg
 * @Date: 2023/5/20 18:09
 * @Version: v1.0
 * @Description:
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    UserMapper userMapper;

    @Override
    public Result sendCode(String phone) {
        //1、验证手机号
        if(!RegexUtils.isPhoneValid(phone)) {
            //2、验证失败，返回错误信息
            return Result.fail("手机号格式错误");
        }

        //3、验证成功，生成验证码
        String authCode = RandomUtil.randomNumbers(6);

        //4、保存验证码到Redis
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY_PREFIX+phone, authCode, LOGIN_CODE_TTL, TimeUnit.MINUTES);

        //5、发送验证码给用户
        log.info("发送验证码成功，验证码是{}", authCode);
        return Result.ok();
    }

    @Override
    public Result loginByCode(LoginFormDto loginFormDto) {
        //1、验证手机号
        String phone = loginFormDto.getPhone();
        if(!RegexUtils.isPhoneValid(phone)) {
            //2、验证失败，返回错误信息
            return Result.fail("手机号格式错误");
        }

        //3、校验验证码
        String code = loginFormDto.getCode();
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY_PREFIX + phone);
        if(cacheCode==null || !cacheCode.equals(code)) {
            //4、验证失败，返回错误信息
            return Result.fail("验证码错误");
        }

        //5、验证成功，根据手机号查询用户
        User user = userMapper.selectUserByPhone(phone);
        if(user == null) {
            //7、用户不存在，创建新用户，保存到数据库
            user = createUserWithPhone(phone);
        }

        //8、将用户信息保存到Redis
        //8.1、随机生成Token，作为登录令牌
        String token = UUID.randomUUID().toString();
        //8.2、将User属性转为map
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(user, userDto);
        Map<String, Object> map = BeanUtil.beanToMap(userDto,new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName,fieldValue)->{
                            if(fieldValue != null) {
                                return fieldValue.toString();
                            }
                            return "";
                        }));
        //8.3、存储
        String tokenKey = LOGIN_TOKEN_KEY_PREFIX+token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, map);
        //8.4、设置token有效期
        stringRedisTemplate.expire(tokenKey, LOGIN_TOKEN_TTL, TimeUnit.MINUTES);

        //9、返回Token给客户端
        return Result.ok(token);
    }

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX+RandomUtil.randomNumbers(10));
        userMapper.insertUser(user);
        return user;
    }
}
