package com.abc.thread.jmm.claude;

public class Singleton {
    // 必须加 volatile，否则指令重排序会导致拿到未初始化的对象
    private static volatile Singleton instance;

    public static Singleton getInstance() {
        if (instance == null) {                    // 第一次检查，不加锁（性能）
            synchronized (Singleton.class) {
                if (instance == null) {            // 第二次检查，加锁（安全）
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }

//            ### happens-before 规则（理解 JMM 的核心）
//
//    JMM 用 **happens-before** 来描述"操作的可见性保证"，记住最重要的 4 条：
//            ```
//            1. 程序顺序规则：同一线程内，前面的操作 happens-before 后面的操作
//
//2. volatile 规则：对 volatile 变量的写 happens-before 后续对它的读
//
//3. 锁规则：unlock happens-before 后续的 lock（所以 synchronized 有可见性保证）
//
//            4. 线程启动规则：thread.start() happens-before 线程内的任何操作
}