package com.abc;

// 两个线程同时对 count++ 操作，结果会小于 2000000
public class Test01_Demo {

    private static Object lock = new Object();
    static int count = 0;

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {

            for (int i = 0; i < 1000000; i++) {
                synchronized (lock) {
                    count++;
                }
            }
            ;
        });
        t1.start();

        Thread t2 = new Thread(() -> {

            for (int i = 0; i < 1000000; i++) {
                synchronized (lock) {
                    count++;
                }
            }
        });
//        t1.start()  →  t1.join() 等t1跑完  →  t2.start()  →  t2.join() 等t2跑完
//        join的意思

        t2.start();
        t1.join();
        t2.join();
/*        正确的修复方式
        count++ 操作本身需要被保护：*/

        System.out.println(count); // 结果不是 2000000！
    }
}