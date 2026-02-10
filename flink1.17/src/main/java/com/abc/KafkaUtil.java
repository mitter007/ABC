package com.abc;

import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;

import java.nio.charset.StandardCharsets;

/**
 * ClassName: KafkaUtil
 * Package: com.abc
 * Description:
 *
 * @Author JWT
 * @Create 2026/2/9 11:36
 * @Version 1.0
 */
public class KafkaUtil {
    KafkaSink<String> sink =
            KafkaSink.<String>builder()
                    .setBootstrapServers("")
                    .setRecordSerializer(
                            KafkaRecordSerializationSchema.<String>builder()
                                    .setTopic("test-topic")
                                    .setKeySerializationSchema(
                                            value -> String.valueOf(value.hashCode()).getBytes(StandardCharsets.UTF_8)
                                    )
                                    .setValueSerializationSchema(
                                            value -> value.getBytes(StandardCharsets.UTF_8)
                                    )
                                    .build()
                    )
                    .build();

}
