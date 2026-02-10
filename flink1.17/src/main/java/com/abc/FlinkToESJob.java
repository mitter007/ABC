package com.abc;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class FlinkToESJob {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();



        env.execute("Flink to ES7.4.2");
    }
}
