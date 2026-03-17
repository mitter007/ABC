package com.abc.thread.sync.exper;

import jdk.nashorn.internal.ir.IfNode;

import java.util.concurrent.*;
import java.util.Random;

/**
 * 🎯 本节练习
 * 模拟一个 多线程日志统计器：
 * <p>
 * 有一个日志处理系统，5 个线程并发解析日志，每个线程处理 10000 条日志，其中随机有一部分是 ERROR 级别。
 * 需要线程安全地统计：总处理条数 和 ERROR 条数，最后打印统计结果。
 */

public class LogCounter {


    private static Object lock = new Object();

    static int totalCount = 0;
    static int errrCount = 0;


    // TODO: 定义 totalCount 和 errorCount，加锁保护它们

    public static void main(String[] args) throws InterruptedException {
        int threadCount = 5;
        int logsPerThread = 10000;

        Random random = new Random();
        // TODO: 创建 5 个线程，每个线程模拟处理 10000 条日志
        // 每条日志有 20% 概率是 ERROR（用 random.nextInt(5) == 0 判断）
        // 线程安全地累加 totalCount 和 errorCount

        Thread t1 = new Thread(() ->
        {
            for (int j = 0; j < 10000; j++) {


                if (random.nextInt(5) == 0) {
                    adderror();
                }
                addtotal();

            }
        }
        );
        Thread t2 = new Thread(() ->
        {
            for (int j = 0; j < 10000; j++) {


                if (random.nextInt(5) == 0) {
                    adderror();
                }
                addtotal();
            }

        }
        );
        Thread t3 = new Thread(() ->
        {
            for (int j = 0; j < 10000; j++) {


                if (random.nextInt(5) == 0) {
                    adderror();
                }
                addtotal();

            }
        }
        );
        Thread t4 = new Thread(() ->
        {
            for (int j = 0; j < 10000; j++) {


                if (random.nextInt(5) == 0) {
                    adderror();
                }
                addtotal();

            }
        }
        );
        Thread t5 = new Thread(() ->
        {
            for (int j = 0; j < 10000; j++) {


                if (random.nextInt(5) == 0) {
                    adderror();
                }
                addtotal();

            }
        }
        );


        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();
        t1.join();
        t2.join();
        t3.join();
        t4.join();
        t5.join();


        // TODO: 等待所有线程完成

        System.out.println(totalCount);
        System.out.println(errrCount);

        // 期望输出：
        // 总处理条数：50000
        // ERROR 条数：约 10000（随机浮动）
    }

    public static synchronized void adderror() {
        errrCount++;
    }

    public static synchronized void addtotal() {
        totalCount++;
    }

//    两节课下来你的并发基础思路是对的，主要要养成避免重复代码和锁要统一的习惯。
//    准备好了回复继续，下一课：volatile 与 JMM 内存模型 ——这是理解并发 bug 根源的关键！
}