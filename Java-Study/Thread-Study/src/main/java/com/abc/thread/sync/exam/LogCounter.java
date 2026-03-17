package com.abc.thread.sync.exam;

import java.util.Random;

public class LogCounter {

    private static final Object lock = new Object();  // 加 final，锁对象不应被替换
    static int totalCount = 0;
    static int errorCount = 0;

    public static void main(String[] args) throws InterruptedException {
        int threadCount = 5;
        int logsPerThread = 10000;
        Random random = new Random();

        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < logsPerThread; j++) {
                    synchronized (lock) {
                        if (random.nextInt(5) == 0) errorCount++;
                        totalCount++;
                    }
                }
            }, "parser-" + i);
        }

        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        System.out.println("总处理条数：" + totalCount);   // 精确 50000
        System.out.println("ERROR 条数：" + errorCount);  // 约 10000
    }
}