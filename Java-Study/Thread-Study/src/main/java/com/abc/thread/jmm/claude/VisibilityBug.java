package com.abc.thread.jmm.claude;

public class VisibilityBug {
//    static boolean running = true;  // 没有 volatile  程序永远无法停止
    static volatile boolean running = true;  // 没有 volatile

//    volatile 做了两件事：
//            ① 保证可见性：写操作立刻刷新到主内存，读操作直接从主内存读
//② 禁止指令重排序：编译器不能把 volatile 变量的读写操作随意移位

    public static void main(String[] args) throws InterruptedException {
        Thread t = new Thread(() -> {
            int i = 0;
            while (running) {  // 可能永远看不到 running=false
                i++;
            }
            System.out.println("线程停止，i=" + i);
        });
        
        t.start();
        Thread.sleep(1000);
        running = false;  // 主线程修改，但工作线程可能看不到！
        System.out.println("已设置 running=false");
    }
}