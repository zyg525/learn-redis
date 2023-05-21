package com.zyg.dto;

import lombok.Data;

/**
 * @Author: zyg
 * @Date: 2023/5/20 18:46
 * @Version: v1.0
 * @Description: 登录表单类
 */
@Data
public class LoginFormDto {
    /**
     * 手机号
     */
    private String phone;
    /**
     * 验证码
     */
    private String code;
    /**
     * 密码
     */
    private String password;
}
