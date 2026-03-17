package com.abc.thread.sync.exper;

import jdk.nashorn.internal.ir.IfNode;

import java.util.Random;

/**
 * 🎯 本节练习
 * 模拟一个 多线程日志统计器：
 * <p>
 * 有一个日志处理系统，5 个线程并发解析日志，每个线程处理 10000 条日志，其中随机有一部分是 ERROR 级别。
 * 需要线程安全地统计：总处理条数 和 ERROR 条数，最后打印统计结果。
 */

public class LogCounter_2 {


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

        Thread[] ts = new Thread[5];
        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(() -> {
                for (int j = 0; j < logsPerThread; j++) {
                    if (random.nextInt(5) == 0) adderror();
                    addtotal();
                }
            });
            thread.setName("thread-" + i);
            ts[i] = thread;
        }
        for (Thread t : ts) {
            t.start();
        }
        for (Thread t : ts) {

            t.join();
        }
        System.out.println(errrCount);

        System.out.println(totalCount);


    }

    public static synchronized void adderror() {
        errrCount++;
    }

    public static synchronized void addtotal() {
        totalCount++;
    }
}