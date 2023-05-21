package com.zyg.dto;

import lombok.Data;

/**
 * @Author: zyg
 * @Date: 2023/5/20 18:46
 * @Version: v1.0
 * @Description: 用户基本信息类
 */
@Data
public class UserDto {
    /**
     * 用户id
     */
    private Long id;
    /**
     * 用户昵称
     */
    private String nickName;
    /**
     * 用户头像
     */
    private String icon;
}
