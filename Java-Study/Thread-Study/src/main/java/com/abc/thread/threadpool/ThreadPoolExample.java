package com.abc.thread.threadpool;

import java.util.concurrent.*;

public class ThreadPoolExample {

    public static void main(String[] args) {

        // 获取CPU核数
        int cpuCores = Runtime.getRuntime().availableProcessors();

        // 自定义线程工厂（方便排查问题）
        ThreadFactory threadFactory = new ThreadFactory() {
            private int count = 0;

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("my-thread-pool-" + count++);
                return t;
            }
        };

        // 自定义拒绝策略
        RejectedExecutionHandler handler = (r, executor) -> {
            System.err.println("任务被拒绝: " + r.toString());
        };

        // 创建线程池（核心）
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                cpuCores,                  // corePoolSize
                cpuCores * 2,              // maximumPoolSize
                60L,                       // keepAliveTime
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100), // 有界队列（防止OOM）
                threadFactory,
                handler
        );

        // 提交任务
        for (int i = 0; i < 20; i++) {
            int taskId = i;

            executor.execute(() -> {
                try {
                    System.out.println(Thread.currentThread().getName() +
                            " 执行任务 " + taskId);

                    Thread.sleep(1000);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // 优雅关闭线程池
        executor.shutdown();

        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}