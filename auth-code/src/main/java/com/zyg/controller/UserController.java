package com.zyg.controller;

import com.zyg.dto.LoginFormDto;
import com.zyg.dto.Result;
import com.zyg.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: zyg
 * @Date: 2023/5/20 18:10
 * @Version: v1.0
 * @Description:
 */
@RestController
public class UserController {
    @Autowired
    UserService userService;

    /**
     * 发送验证码
     * @return
     */
    @RequestMapping("/sendCode")
    public Result sendAuthCode(@RequestParam("phone") String phone) {
        return userService.sendCode(phone);
    }

    /**
     * 使用验证码登录或注册
     * @param loginFormDto
     * @return
     */
    @RequestMapping("/loginByCode")
    public Result loginOrRegisterByCode(LoginFormDto loginFormDto) {
        return userService.loginByCode(loginFormDto);
    }

    /**
     * 使用token免登录访问
     * @param loginFormDto
     * @return
     */
    @RequestMapping("/access")
    public Result accessByToken(LoginFormDto loginFormDto) {
        return Result.ok("token访问成功");
    }
}
