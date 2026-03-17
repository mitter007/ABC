package com.abc;

import com.sun.org.apache.bcel.internal.generic.NEW;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * ClassName: Thread_01Callable
 * Package: com.abc
 * Description:
 *
 * @Author JWT
 * @Create 2026/3/16 18:58
 * @Version 1.0
 */
public class Thread_01Callable {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Callable<Integer> task = () -> {
            Thread.sleep(1000);
            return 42; // 有返回值
        };
        FutureTask<Integer> future = new FutureTask<>(task);
        new Thread(future).start();
        Integer result = future.get(); // 阻塞等待结果
        System.out.println("结果：" + result);


//        ---
//
//### 线程生命周期
//```
//        NEW → RUNNABLE → BLOCKED/WAITING/TIMED_WAITING → TERMINATED
//        新建     运行中          阻塞/等待                    结束
    }
}
