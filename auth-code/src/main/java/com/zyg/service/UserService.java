package com.zyg.service;

import com.zyg.dto.LoginFormDto;
import com.zyg.dto.Result;

/**
 * @Author: zyg
 * @Date: 2023/5/20 18:09
 * @Version: v1.0
 * @Description:
 */
public interface UserService {

    Result sendCode(String phone);

    Result loginByCode(LoginFormDto loginFormDto);
}
