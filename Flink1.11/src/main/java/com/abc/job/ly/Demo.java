package com.abc.job.ly;



//**内存可见性问题（Memory Visibility Problem）**是并发编程中的一个核心概念，指的是：
//        👉 一个线程对共享变量所做的修改，另一个线程不能及时、甚至永远看不到。
//
//这不是逻辑错误，而是CPU缓存、编译器优化、指令重排等共同作用的结果。
//
//一、问题本质
//
//在多线程环境中：
//
//每个线程都有自己的工作内存（寄存器、CPU缓存等）
//
//共享变量实际存储在主内存
//
//线程可能从自己的缓存中读取变量，而不是主内存
//
//因此可能出现：
//
//线程 A 修改了变量 x，但线程 B 仍然看到的是旧值。
class Demo {
    private static boolean flag = false;

    public static void main(String[] args) throws InterruptedException {
        new Thread(() -> {
            while (!flag) {
                // 空循环
            }
            System.out.println("线程1结束");
        }).start();

        Thread.sleep(1000);
        flag = true;
        System.out.println("主线程已修改 flag");
    }
}
