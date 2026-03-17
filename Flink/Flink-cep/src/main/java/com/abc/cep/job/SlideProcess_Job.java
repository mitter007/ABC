package com.abc.cep.job;

import com.abc.pojo.LoginEvent;
import com.sun.org.apache.xerces.internal.dom.PSVIAttrNSImpl;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.time.Duration;
import java.util.HashMap;

/**
 * ClassName: SlideProcess_Job
 * Package: com.abc.cep.job
 * Description:
 *
 * @Author JWT
 * @Create 2026/3/13 16:33
 * @Version 1.0
 */
public class SlideProcess_Job {
    public static void main(String[] args) {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers("brokers")
                .setTopics("input-topic")
                .setGroupId("my-group")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        DataStreamSource<String> source1 = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source");
        SingleOutputStreamOperator<HashMap<String, Object>> stream = source1.map(new MapFunction<String, HashMap<String, Object>>() {
            @Override
            public HashMap<String, Object> map(String value) throws Exception {
                return new HashMap<>();
            }
        });


        stream.keyBy(e->e.get("id"))
                .window(SlidingProcessingTimeWindows.of(Time.seconds(150),Time.seconds(
                        60
                )))
                .evictor(new KpiLIineEvictor<>(150,90));



    }
}
