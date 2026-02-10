package com.abc.job;

import com.abc.errorcode.AlarmRule;
import com.abc.errorcode.MetricEvent;
import com.abc.errorcode.RuleState;
import com.abc.errorcode.TotalDrivenAlarmProcess;
import com.abc.errorcode.source.HttpPollingSource;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

import java.time.Duration;
import java.util.Properties;

public class AlarmJob {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env =
            StreamExecutionEnvironment.getExecutionEnvironment();

        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        env.enableCheckpointing(60000);
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("group.id", "test");
        DataStream<String> stream = env
                .addSource(new FlinkKafkaConsumer<>("topic", new SimpleStringSchema(), properties));

        SingleOutputStreamOperator<MetricEvent> map = stream.map(new MapFunction<String, MetricEvent>() {
            @Override
            public MetricEvent map(String s) throws Exception {
                return new MetricEvent();
            }
        });
        DataStream<MetricEvent> metricStream =
                map
               .assignTimestampsAndWatermarks(
                   WatermarkStrategy
                       .<MetricEvent>forBoundedOutOfOrderness(Duration.ofSeconds(5))
                       .withTimestampAssigner((e, ts) -> e.windowEnd)
               );

        DataStream<AlarmRule> ruleStream =
            env.addSource(new HttpPollingSource( 5000))
               .setParallelism(1);

        BroadcastStream<AlarmRule> ruleBroadcast =
            ruleStream.broadcast(RuleState.RULE_STATE);

        metricStream
            .keyBy(e ->
                e.syscode + "|" +
                e.groupcode + "|" +
                e.firstDim + "|" +
                e.windowStart + "|" +
                e.windowEnd
            )
            .connect(ruleBroadcast)
            .process(new TotalDrivenAlarmProcess())
            .print();

        env.execute("Flink 1.11 TOTAL Driven Alarm Job");
    }
}
