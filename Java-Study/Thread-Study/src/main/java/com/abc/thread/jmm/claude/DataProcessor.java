package com.abc.thread.jmm.claude;

// Flink / Spark 自定义算子里经常见到这种模式
public class DataProcessor {
    private volatile boolean stopped = false;

    public void process() {
        while (!stopped) {
            // 持续处理数据...
        }
    }

    public void stop() {
        stopped = true;  // 其他线程调用，立刻让 process 感知到
    }
}