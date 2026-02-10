package com.atguigu.es;


import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.util.Collector;

import java.time.Duration;
import java.util.Properties;

// ================== 输入数据模型 ==================
@Data
class AIDateRecord {
    private String syscode;
    private String groupcode;
    private long timestamp;
    private Dimension dimension;
    private int tps_60;

    @Data
    public static class Dimension {
        private String transCode;
        private String errorCode;
    }
}

// ================== 聚合结果模型 ==================
@Data
class Result {
    private String syscode;
    private String groupcode;
    private String transCode;
    private String errorCode;
    private long windowStart;
    private long windowEnd;
    private int totalTps;
    private Integer windowSize; // 窗口大小（分钟）
}

// ================== 聚合逻辑 ==================
class TpsAggregateFunction implements AggregateFunction<AIDateRecord, Integer, Integer> {
    @Override
    public Integer createAccumulator() {
        return 0;
    }

    @Override
    public Integer add(AIDateRecord value, Integer accumulator) {
        return accumulator + value.getTps_60();
    }

    @Override
    public Integer getResult(Integer accumulator) {
        return accumulator;
    }

    @Override
    public Integer merge(Integer a, Integer b) {
        return a + b;
    }
}

// ================== 窗口处理函数（能拿到窗口时间） ==================
class TpsWindowFunction extends ProcessWindowFunction<Integer, Result, Tuple4<String, String, String, String>, TimeWindow> {

    private final int windowSize;

    public TpsWindowFunction(int windowSize) {
        this.windowSize = windowSize;
    }

    @Override
    public void process(Tuple4<String, String, String, String> key,
                        Context context,
                        Iterable<Integer> elements,
                        Collector<Result> out) {

        Integer total = elements.iterator().next();

        Result result = new Result();
        result.setSyscode(key.f0);
        result.setGroupcode(key.f1);
        result.setTransCode(key.f2);
        result.setErrorCode(key.f3);
        result.setWindowStart(context.window().getStart());
        result.setWindowEnd(context.window().getEnd());
        result.setTotalTps(total);
        result.setWindowSize(windowSize);

        out.collect(result);
    }
}

// ================== 主程序 ==================
public class FlinkKafkaAggregateToKafka {
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(3);

        // -------- 1. Kafka Source 配置 --------
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", "localhost:9092");
        props.setProperty("group.id", "ai_data_consumer");

        FlinkKafkaConsumer<String> kafkaSource = new FlinkKafkaConsumer<>(
                "AI_DATA",
                new org.apache.flink.api.common.serialization.SimpleStringSchema(),
                props
        );
        kafkaSource.setStartFromLatest();

        DataStreamSource<String> source = env.addSource(kafkaSource);

        // -------- 2. JSON 解析 + 时间戳提取 --------
        SingleOutputStreamOperator<AIDateRecord> parsed = source
                .map(new MapFunction<String, AIDateRecord>() {
                    @Override
                    public AIDateRecord map(String value) {
                        return JSONObject.parseObject(value, AIDateRecord.class);
                    }
                })
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<AIDateRecord>forBoundedOutOfOrderness(Duration.ofSeconds(10))
                                .withTimestampAssigner((event, ts) -> event.getTimestamp())
                );

        // -------- 3. KeyBy --------
        KeyedStream<AIDateRecord, Tuple4<String, String, String, String>> keyed = parsed
                .keyBy(record -> Tuple4.of(
                        record.getSyscode(),
                        record.getGroupcode(),
                        record.getDimension().getTransCode(),
                        record.getDimension().getErrorCode()
                ));

        // -------- 4. 窗口计算 --------
        SingleOutputStreamOperator<Result> result5min = keyed
                .window(TumblingEventTimeWindows.of(Time.minutes(5)))
                .aggregate(new TpsAggregateFunction(), new TpsWindowFunction(5));

        SingleOutputStreamOperator<Result> result15min = keyed
                .window(TumblingEventTimeWindows.of(Time.minutes(15)))
                .aggregate(new TpsAggregateFunction(), new TpsWindowFunction(15));




        env.execute("AI_DATA TPS Aggregate To Kafka");
    }
}
