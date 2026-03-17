package com.abc.thread.callable;// 提示：框架如下，你来补全

import java.util.concurrent.*;
import java.util.Random;

public class PartitionTask {
    public static void main(String[] args) throws Exception {
        Random random = new Random();

        // TODO: 创建 3 个 Callable，每个返回一个随机数据量（1000~9999）



        Callable<Integer> c1 = new Callable() {
            @Override
            public Integer call() throws Exception {
                return random.nextInt(9000) + 1000;

            }
        };
        Callable<Integer> c2 = new Callable() {
            @Override
            public Integer call() throws Exception {
                return random.nextInt(9000) + 1000;

            }
        };
        Callable<Integer> c3 = new Callable() {
            @Override
            public Integer call() throws Exception {
                return random.nextInt(9000) + 1000;

            }
        };
        // TODO: 用 FutureTask 包装并启动线程
        FutureTask<Integer> f1 = new FutureTask<>(c1);
        FutureTask<Integer> f2 = new FutureTask<>(c2);
        FutureTask<Integer> f3 = new FutureTask<>(c3);
        Thread t1 = new Thread(f1);
        Thread t2 =  new Thread(f2);
        Thread t3 = new Thread(f3);
        t1.start();
        t2.start();
        t3.start();
        t1.sleep((random.nextInt(2)+1)*1000);
        t2.sleep((random.nextInt(2)+1)*1000);
        t3.sleep((random.nextInt(2)+1)*1000);
        t1.join();
        t2.join();
        t3.join();


        Integer i1 = f1.get();
        Integer i2 = f2.get();
        Integer i3 = f3.get();
        Integer total = i1 + i2 + i3;



//        3 个线程分别代表 partition-0、partition-1、partition-2
//        每个线程随机 sleep 1~3 秒（模拟处理耗时）
//        主线程等待全部完成后打印总数据量
        // TODO: 主线程汇总三个结果并打印


        System.out.println("总数据量：" + total);
    }
}