package com.abc.spark;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.apache.spark.api.java.function.Function;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * ClassName: Test01_HelloWorld_yarn
 * Package: com.abc.spark
 * Description:
 *
 * @Author JWT
 * @Create 2025/10/13 19:09
 * @Version 1.0
 */
public class Test01_HelloWorld_Yarn {
    public static void main(String[] args) throws InterruptedException {

        // ✅ 1. 创建 SparkConf（注意这里不能用 "local[*]"）
        SparkConf conf = new SparkConf()
                .setAppName("KafkaStreamingOnYarn");

        // ⚠️ 不要设置 setMaster("local[*]")，YARN 提交时会自动接管

        // ✅ 2. 创建 StreamingContext（批处理间隔为3秒）
        JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(3));

        // ✅ 3. Kafka 参数配置
        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "hadoop204:9092");
        kafkaParams.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaParams.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaParams.put(ConsumerConfig.GROUP_ID_CONFIG, "atguigu");
        kafkaParams.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        kafkaParams.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        // ✅ 4. 订阅的 topic 列表
        Collection<String> topics = Arrays.asList("topic_db");

        // ✅ 5. 创建 Kafka 流
        JavaInputDStream<ConsumerRecord<String, String>> kafkaStream =
                KafkaUtils.createDirectStream(
                        jssc,
                        LocationStrategies.PreferConsistent(),
                        ConsumerStrategies.Subscribe(topics, kafkaParams)
                );

        // ✅ 6. 处理逻辑
        kafkaStream.map((Function<ConsumerRecord<String, String>, String>) ConsumerRecord::value)
                .print(100);

        // ✅ 7. 启动流式计算
        jssc.start();
        jssc.awaitTermination();
    }
}
