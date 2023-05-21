package com.zyg.dao;

import com.zyg.entity.Shop;
import com.zyg.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @Author: zyg
 * @Date: 2023/5/20 18:10
 * @Version: v1.0
 * @Description:
 */
@Mapper
public interface ShopMapper {
    @Select("SELECT id, name, type_id AS typeId, images, area, address, x, y, avg_price AS avgPrice, " +
            "sold, comments, score, open_hours AS openHours, create_time AS createTime, update_time AS updateTime " +
            "FROM tb_shop WHERE id = #{id}")
    Shop selectShopById(Long id);

    @Update("UPDATE tb_shop SET name = #{name} WHERE id = #{id}")
    Integer updateShopById(Long id, String name);
}
