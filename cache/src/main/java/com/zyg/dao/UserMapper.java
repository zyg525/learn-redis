package com.zyg.dao;

import com.zyg.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @Author: zyg
 * @Date: 2023/5/20 18:10
 * @Version: v1.0
 * @Description:
 */
@Mapper
public interface UserMapper {
    @Select("SELECT id, phone, password, nick_name AS nickName, icon, " +
            "create_time AS createTime, update_time AS updateTime " +
            "FROM tb_user WHERE phone = #{phone}")
    User selectUserByPhone(String phone);

    @Insert("INSERT INTO tb_user " +
            "(id, phone, password, nick_name, icon, create_time, update_time) " +
            "VALUES " +
            "(#{id}, #{phone}, #{password}, #{nickName}, #{icon}, #{createTime}, #{updateTime})")
    Integer insertUser(User user);
}
