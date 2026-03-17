package com.abc.thread.sync.exam;

/**
 * synchronized 的 3 种用法
 * 用法一：锁普通方法（锁的是 this 对象）
 * javapublic synchronized void add() {
 *     count++;
 * }
 * 用法二：锁静态方法（锁的是 Class 对象）
 * javapublic static synchronized void add() {
 *     count++;
 * }
 * 用法三：锁代码块（锁的是指定对象，最灵活）
 * javaprivate final Object lock = new Object();
 *
 * public void add() {
 *     // 只锁关键代码，性能更好
 *     synchronized (lock) {
 *         count++;
 *     }
 * }
 */

public class Counter {
    private int count = 0;
    private final Object lock = new Object();

    public void add() {
        synchronized (lock) { count++; }
    }

    public static void main(String[] args) {
        // 两个线程用同一个 Counter 实例 → 互斥，安全 ✅
        Counter c = new Counter();
        new Thread(() -> c.add()).start();
        new Thread(() -> c.add()).start();

// 两个线程用不同 Counter 实例 → 各自拿各自的锁，不互斥 ❌
        Counter c1 = new Counter();
        Counter c2 = new Counter();
        new Thread(() -> c1.add()).start();
        new Thread(() -> c2.add()).start();

        c1.methodA();



//        记住：两个线程必须抢同一把锁，互斥才有效。
    }


    public synchronized void methodA() {
        methodB(); // 调用同样被 synchronized 修饰的方法
    }

    public synchronized void methodB() {
        // 同一线程可以再次进入，不会卡住
        System.out.println("可重入");
    }
}