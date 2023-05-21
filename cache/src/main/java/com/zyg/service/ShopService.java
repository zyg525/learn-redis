package com.zyg.service;

import com.zyg.dto.Result;
import com.zyg.entity.Shop;

/**
 * @Author: zyg
 * @Date: 2023/5/21 14:18
 * @Version: v1.0
 * @Description:
 */
public interface ShopService {
    Result queryShopById(Long id);

    Result updateShopNameById(Long id, String name);

    Shop queryWithMutex(Long id);

    Shop queryWithLogicalExpire(Long id);
}
