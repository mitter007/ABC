package com.abc.job.performancelogjob;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;
import redis.clients.jedis.JedisCluster;

import java.util.Properties;

/**
 * ClassName: kafkaJob
 * Package: com.abc.job.performancelogjob
 * Description:
 *
 * @Author JWT
 * @Create 2026/2/5 16:19
 * @Version 1.0
 */
public class kafkaJob {
    public static void main(String[] args) throws Exception {

        Configuration conf = new Configuration();
        conf.setString("rest.port", "1000");
        StreamExecutionEnvironment env =
                StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf);
        env.setParallelism(3);


//        env.enableCheckpointing(60000);
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "hadoop202:9092");
        properties.setProperty("group.id", "flink-11");
        DataStream<String> stream = env
                .addSource(new FlinkKafkaConsumer<>("anti", new SimpleStringSchema(), properties));

        stream.print("stream>>>");
        SingleOutputStreamOperator<String> process = stream.process(new ProcessFunction<String, String>() {
            @Override
            public void processElement(String value, ProcessFunction<String, String>.Context ctx, Collector<String> out) throws Exception {
                JedisCluster jedisCluster = JedisClusterUtil.getJedisCluster();
                String s = jedisCluster.get("abc");
                System.out.println(s);
                out.collect(value);
            }
        });

        process.print();
        env.execute();

    }
}
