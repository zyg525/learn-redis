package com.zyg.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: zyg
 * @Date: 2023/5/21 14:27
 * @Version: v1.0
 * @Description: 商铺类
 */
@Data
public class Shop {
    private Integer id;
    private String name;
    private Integer typeId;
    private String images;
    private String area;
    private String address;
    private Double x;
    private Double y;
    private Double avgPrice;
    private Integer sold;
    private Integer comments;
    private Integer score;
    private String openHours;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
