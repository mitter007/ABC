package com.abc.thread.callable;

import java.util.concurrent.*;
import java.util.Random;

public class PartitionTask_Claude {
    public static void main(String[] args) throws Exception {
        Random random = new Random();

        Callable<Integer> c1 = () -> {
            Thread.sleep((random.nextInt(3) + 1) * 1000);
            int count = random.nextInt(9000) + 1000;
            System.out.println(Thread.currentThread().getName() + " 处理完成，数据量：" + count);
            return count;
        };
        Callable<Integer> c2 = () -> {
            Thread.sleep((random.nextInt(3) + 1) * 1000);
            int count = random.nextInt(9000) + 1000;
            System.out.println(Thread.currentThread().getName() + " 处理完成，数据量：" + count);
            return count;
        };
        Callable<Integer> c3 = () -> {
            Thread.sleep((random.nextInt(3) + 1) * 1000);
            int count = random.nextInt(9000) + 1000;
            System.out.println(Thread.currentThread().getName() + " 处理完成，数据量：" + count);
            return count;
        };

        FutureTask<Integer> f1 = new FutureTask<>(c1);
        FutureTask<Integer> f2 = new FutureTask<>(c2);
        FutureTask<Integer> f3 = new FutureTask<>(c3);

        new Thread(f1, "partition-0").start();
        new Thread(f2, "partition-1").start();
        new Thread(f3, "partition-2").start();

        // join 保证主线程等待所有线程完成（其实 f.get() 本身也会阻塞，join 可省略）
        int total = f1.get() + f2.get() + f3.get();

        System.out.println("总数据量：" + total);
    }
}