package com.zyg.controller;

import com.zyg.dto.Result;
import com.zyg.entity.Shop;
import com.zyg.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: zyg
 * @Date: 2023/5/21 14:17
 * @Version: v1.0
 * @Description:
 */
@RestController
public class ShopController {
    @Autowired
    ShopService shopService;

    @RequestMapping("/shop/query")
    public Result queryShopById(@RequestParam("id") Long id) {
        return shopService.queryShopById(id);
    }

    @RequestMapping("/shop/update")
    public Result updateShopNameById(@RequestParam("id") Long id, @RequestParam("name") String name) {
        return shopService.updateShopNameById(id, name);
    }
}
