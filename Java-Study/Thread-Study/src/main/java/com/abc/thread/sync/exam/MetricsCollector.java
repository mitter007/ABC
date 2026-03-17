package com.abc.thread.sync.exam;

public class MetricsCollector {
    private long processedCount = 0;
    private long errorCount = 0;
    private final Object lock = new Object();

    // 多个线程并发调用，统计处理数量
    public void recordProcessed(int count) {
        synchronized (lock) {
            processedCount += count;
        }
    }

    public void recordError() {
        synchronized (lock) {
            errorCount++;
        }
    }

    public void printStats() {
        synchronized (lock) {
            System.out.println("处理总量：" + processedCount + "，错误数：" + errorCount);
        }
    }
}