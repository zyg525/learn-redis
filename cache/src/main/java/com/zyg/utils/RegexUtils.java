package com.zyg.utils;

import cn.hutool.core.util.StrUtil;

/**
 * @Author: zyg
 * @Date: 2023/5/20 18:22
 * @Version: v1.0
 * @Description: 格式验证工具
 */
public class RegexUtils {
    /**
     * 验证手机号格式是否正确
     * @param phone
     * @return
     */
    public static boolean isPhoneValid(String phone) {
        return isMatch(phone, RegexPatterns.PHONE_REGEX);
    }

    private static boolean isMatch(String str, String regex) {
        if(StrUtil.isBlank(str)) {
            return true;
        }
        return str.matches(regex);
    }
}
