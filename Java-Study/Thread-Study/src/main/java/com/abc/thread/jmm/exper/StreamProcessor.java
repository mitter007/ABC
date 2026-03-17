package com.abc.thread.jmm.exper;

//🎯 本节练习
//模拟一个 Flink 风格的流处理任务控制器：
//
//一个数据处理线程持续消费数据（每次处理模拟 sleep 200ms），主线程在 2 秒后发出停止信号，处理线程能正确感知并优雅退出，打印总共处理了多少条数据。

import jdk.nashorn.internal.ir.Flags;

public class StreamProcessor {

    // TODO 1: 声明停止标志，思考是否需要 volatile
    private volatile static boolean flag = true;
    // TODO 2: 声明处理计数器，思考是否需要 volatile 或 synchronized
    private static int count;

    public static void main(String[] args) throws InterruptedException {

        Thread processor = new Thread(() -> {
            // TODO 3: 循环处理数据，每次 sleep 200ms 模拟处理耗时
            while (flag) {
                count++;
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            // 感知到停止信号后退出循环，打印处理条数
        }, "data-processor");

        processor.start();

        Thread.sleep(2000);  // 主线程等 2 秒

        // TODO 4: 发出停止信号
        flag = false;

        processor.join();
        System.out.println("处理器已停止");
        System.out.println("总共打印了" + count + "条数据");
    }
}