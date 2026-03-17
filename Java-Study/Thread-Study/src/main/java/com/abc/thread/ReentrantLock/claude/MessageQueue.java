package com.abc.thread.ReentrantLock.claude;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MessageQueue {
    private final Queue<String> queue = new LinkedList<>();
    private final int capacity = 10;
    private final ReentrantLock lock = new ReentrantLock();

    // 两个条件：队列不满 / 队列不空
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();

    // 生产者：队列满了就等待
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

    // 消费者：队列空了就等待
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
}