package com.abc.thread.ReentrantLock.exec;

import java.util.concurrent.locks.*;
import java.util.*;

// 本节练习
//模拟一个 Kafka 风格的有界消息队列：
//生产者线程不断往队列写消息，消费者线程不断从队列取消息处理。队列容量上限为 5。
// 生产者每 300ms 生产一条，消费者每 500ms 消费一条。
// 运行 5 秒后主线程停止所有操作，打印总生产数和总消费数。

public class BoundedQueue {

    private final Queue<String> queue = new LinkedList<>();
    private final int capacity = 5;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();

    private static volatile boolean running = true;

    // TODO 1: 实现 put 方法，队列满时等待
    public void put(String msg) throws InterruptedException {
        lock.lock();
        try {
            while (queue.size() == capacity) {
                notFull.await();  // 等待"不满"信号
            }
            queue.offer(msg);
            notEmpty.signal();    // 通知消费者"不空了"
        } finally {
            lock.unlock();
        }
    }

    // TODO 2: 实现 take 方法，队列空时等待，返回消息
    public String take() throws InterruptedException {

        lock.lock();
        String msg;
        try {
            while (queue.isEmpty()) {
                notEmpty.await(); // 等待"不空"信号
            }
            msg = queue.poll();
            notFull.signal();     // 通知生产者"不满了"
        } finally {
            lock.unlock();
        }
        return msg; // 放在 finally 外，已经安全
    }

    public static void main(String[] args) throws InterruptedException {
        BoundedQueue mq = new BoundedQueue();
        int[] produced = {0};  // 用数组包装，Lambda 内可修改
        int[] consumed = {0};

        // TODO 3: 启动生产者线程，每 300ms 生产一条消息 "msg-N"
        new Thread(() -> {
            while (running) {
                try {
                    mq.put("msg-" + produced[0]);
                    produced[0]++;
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }

        }).start();

        // TODO 4: 启动消费者线程，每 500ms 消费一条消息并打印

        new Thread(() -> {
            while (running) {
                try {
                    String msg = mq.take();
                    System.out.println(msg);
                    consumed[0]++;
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }

        }).start();

        Thread.sleep(5000);  // 运行 5 秒

        // TODO 5: 停止所有线程
        running = false;


        System.out.println("总生产：" + produced[0]);
        System.out.println("总消费：" + consumed[0]);
    }
}