package com.abc.thread.ReentrantLock.claude;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Counter {
    private int count = 0;
    private final ReentrantLock lock = new ReentrantLock();

    // 默认是非公平锁（性能好）
    ReentrantLock fairLock = new ReentrantLock(true);   // true = 公平锁，按等待顺序获取
    ReentrantLock unfairLock = new ReentrantLock(false); // false = 非公平锁（默认）

    public void add() {
        lock.lock();       // 加锁
        try {
            count++;       // 临界区
        } finally {
            lock.unlock(); // ⚠️ 必须在 finally 里释放，否则异常会导致死锁
        }
    }

    public void add2() {
        if (lock.tryLock()) {  // 尝试获取锁，立即返回
            try {
                count++;
            } finally {
                lock.unlock();
            }
        } else {
            System.out.println("锁被占用，先去做别的事");
        }
    }
    public void add3() throws InterruptedException {
        // 最多等 500ms，超时就放弃
        if (lock.tryLock(500, TimeUnit.MILLISECONDS)) {
            try {
                count++;
            } finally {
                lock.unlock();
            }
        } else {
            System.out.println("等待超时，放弃本次操作");
        }
    }
    public void add4() throws InterruptedException {
        lock.lockInterruptibly();  // 等待锁时可以被 interrupt() 中断
        try {
            count++;
        } finally {
            lock.unlock();
        }
    }
}