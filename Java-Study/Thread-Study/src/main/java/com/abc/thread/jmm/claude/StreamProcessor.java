package com.abc.thread.jmm.claude;

public class StreamProcessor {

    private static volatile boolean flag = true;
    private static int count = 0;  // 只有一个线程写，不需要加锁

    public static void main(String[] args) throws InterruptedException {

        Thread processor = new Thread(() -> {
            while (flag) {
                count++;
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();  // 最佳实践：恢复中断标志
                    break;
                }
            }
            // 在处理线程内打印
            System.out.println(Thread.currentThread().getName()
                    + " 已停止，共处理 " + count + " 条数据");
        }, "data-processor");

        processor.start();
        Thread.sleep(2000);

        flag = false;  // 发出停止信号

        processor.join();
        System.out.println("处理器已停止");
    }
}