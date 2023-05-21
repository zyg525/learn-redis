package com.zyg.utils;

import com.zyg.dto.UserDto;

/**
 * @Author: zyg
 * @Date: 2023/5/21 10:51
 * @Version: v1.0
 * @Description: ThreadLocal工具类
 */
public class UserHolder {
    private static final ThreadLocal<UserDto> tl = new ThreadLocal();

    public static void saveUserDto(UserDto userDto) {
        tl.set(userDto);
    }

    public static UserDto getUserDto() {
        return tl.get();
    }

    public static void removeUserDto() {
        tl.remove();
    }
}
