package com.abc.redis;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Objects;

/**
 * ClassName: FlinkMapTest
 * Package: com.abc.redis
 * Description:
 *
 * @Author JWT
 * @Create 2026/2/9 15:40
 * @Version 1.0
 */
public class FlinkMapTest {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<String> kakfaSource = env.socketTextStream("hadoop202", 9999);
        kakfaSource.map(

                new RichMapFunction<String, String>() {
                    long start;

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        start = System.currentTimeMillis();
                        System.out.println("start:"+start);

                        new Thread(() -> {
                            System.out.println("子线程开始");
                            throw new RuntimeException("子线程炸了");
                        }).start();

                    }

                    @Override
                    public String map(String s) throws Exception {
                        return JedisClusterUtil1.get("a");
                    }
                }
        ).filter(Objects::nonNull).print();

        env.execute();
    }
}
