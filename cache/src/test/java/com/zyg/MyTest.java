package com.zyg;

import com.zyg.utils.RedisWorker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @Author: zyg
 * @Date: 2023/5/22 9:59
 * @Version: v1.0
 * @Description: 测试类
 */
@SpringBootTest
public class MyTest {

    @Autowired
    RedisWorker redisWorker;

    @Test
    public void test1() {
        ExecutorService executorService = Executors.newFixedThreadPool(1000);
        ArrayList<Future> list = new ArrayList<>();

        for(int i=0;i<1000;i++) {
            Future<Long> future = executorService.submit(() -> {
                Long order = redisWorker.nextId("order");
                return order;
            });
            list.add(future);
        }

        HashSet<Long> set = new HashSet<>();

        for(Future<Long> future:list) {
            try {
                Long id = future.get();
                System.out.println(id);
                set.add(id);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        System.out.println("共生成id："+set.size()+"个");
    }

    @Test
    public void test2() {
        Long id = redisWorker.nextId("order");
        System.out.println(id);
    }

    @Test
    public void test3() {
        Long a = -1682979884407L<<32 | 1002;
        String s = Long.toBinaryString(a);
        System.out.println(s);
    }
}
