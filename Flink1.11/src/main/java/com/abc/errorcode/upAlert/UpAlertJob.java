package com.abc.errorcode.upAlert;

import com.abc.errorcode.upAlert.entity.Alarm;
import com.abc.errorcode.upAlert.entity.AlarmLevel;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.functions.PatternProcessFunction;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.util.*;

/**
 * ClassName: UpAlertJob
 * Package: com.abc.errorcode.upAlert
 * Description:
 *
 * @Author JWT
 * @Create 2026/3/24 9:20
 * @Version 1.0
 */
public class UpAlertJob {
    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Map<String, Integer> ruleMap = new HashMap<>();

        ruleMap.put("A_G1", 3);
        ruleMap.put("A_G2", 5);
        ruleMap.put("B_G1", 10);


        OutputTag<Alarm> tag3 = new OutputTag<Alarm>("3min") {
        };
        OutputTag<Alarm> tag5 = new OutputTag<Alarm>("5min") {
        };
        OutputTag<Alarm> tag10 = new OutputTag<Alarm>("10min") {
        };

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("group.id", "test");
        SingleOutputStreamOperator<Alarm> stream = env.addSource(new FlinkKafkaConsumer<>("topic", new SimpleStringSchema(), properties))
                .map(s -> {
                    JSONObject obj = JSON.parseObject(s);

                    Alarm alarm = new Alarm();
                    alarm.setSyscode(obj.getString("syscode"));
                    alarm.setGroupcode(obj.getString("groupcode"));
                    alarm.setTs(obj.getLong("ts"));

                    return alarm;
                })
                .filter(Objects::nonNull);

        SingleOutputStreamOperator<Alarm> mainStream =
                stream.process(new ProcessFunction<Alarm, Alarm>() {

                    @Override
                    public void processElement(
                            Alarm value,
                            Context ctx,
                            Collector<Alarm> out) {

                        String key = value.getSyscode() + "_" + value.getGroupcode();

                        Integer duration = ruleMap.get(key);

                        if (duration == null) return;

                        switch (duration) {
                            case 3:
                                ctx.output(tag3, value);
                                break;
                            case 5:
                                ctx.output(tag5, value);
                                break;
                            case 10:
                                ctx.output(tag10, value);
                                break;
                        }
                    }
                });
        DataStream<Alarm> stream3 = mainStream.getSideOutput(tag3);

        SingleOutputStreamOperator<AlarmLevel> level1 =
                buildCep(stream3, 3, 2);
        DataStream<Alarm> stream5 = mainStream.getSideOutput(tag5);

        SingleOutputStreamOperator<AlarmLevel> level2 =
                buildCep(stream5, 5, 4);

        DataStream<Alarm> stream10 = mainStream.getSideOutput(tag10);

        SingleOutputStreamOperator<AlarmLevel> level3 =
                buildCep(stream10, 10, 9);
        DataStream<AlarmLevel> result =
                level1.union(level2).union(level3);


        SingleOutputStreamOperator<AlarmLevel> level4 =
                buildCepCount(stream, 3, 3, 1);   // 3分钟 ≥3次

        SingleOutputStreamOperator<AlarmLevel> level5 =
                buildCepCount(stream, 5, 5, 2);   // 5分钟 ≥5次

        SingleOutputStreamOperator<AlarmLevel> level6 =
                buildCepCount(stream, 10, 10, 3); // 10分钟 ≥10次

        DataStream<AlarmLevel> result1 =
                level1.union(level2).union(level3);


    }

    public static SingleOutputStreamOperator<AlarmLevel> buildCep(
            DataStream<Alarm> stream,
            int minutes, int times
    ) {

        // 1️⃣ keyBy（建议用 Tuple，避免字符串拼接）
        KeyedStream<Alarm, Tuple4<String, String, String, String>> keyedStream =
                stream.keyBy(a -> Tuple4.of(a.getSyscode(), a.getGroupcode(), a.getFirstDim(), a.getType()));

        // 2️⃣ 定义 Pattern（⚠️ 不要用 <?>）
        Pattern<Alarm, Alarm> pattern = Pattern
                .<Alarm>begin("start", AfterMatchSkipStrategy.skipPastLastEvent())
                .next("next")                  // 严格连续（告警场景推荐）
                .timesOrMore(times)
                .within(Time.minutes(minutes));

        // 3️⃣ 应用 CEP
        PatternStream<Alarm> patternStream = CEP.pattern(keyedStream, pattern);

        // 4️⃣ 处理匹配结果
        return patternStream.process(
                new PatternProcessFunction<Alarm, AlarmLevel>() {

                    @Override
                    public void processMatch(
                            Map<String, List<Alarm>> match,
                            Context ctx,
                            Collector<AlarmLevel> out) {

                        // start 一定有
                        Alarm first = match.get("start").get(0);

                        // next 可能为空（但一般 oneOrMore 后不会）
                        List<Alarm> nextList = match.get("next");

                        Alarm last;
                        if (nextList != null && !nextList.isEmpty()) {
                            last = nextList.get(nextList.size() - 1);
                        } else {
                            last = first;
                        }

                        long duration = last.getTs() - first.getTs();

                        // 再做一次兜底判断（防 CEP 边界误触发）
                        if (duration >= minutes * 60 * 1000L) {
                            out.collect(new AlarmLevel(
                                    first.getSyscode(),
                                    first.getGroupcode(),
                                    1,
                                    duration
                            ));
                        }
                    }
                }
        );
    }

    public static SingleOutputStreamOperator<AlarmLevel> buildCepCount(
            DataStream<Alarm> stream,
            int minutes,
            int threshold,   // 次数阈值
            int level) {

        KeyedStream<Alarm, Tuple4<String, String, String, String>> keyedStream =
                stream.keyBy(a -> Tuple4.of(
                        a.getSyscode(),
                        a.getGroupcode(),
                        a.getFirstDim(),
                        a.getType()
                ));

        Pattern<Alarm, Alarm> pattern = Pattern
                .<Alarm>begin("start", AfterMatchSkipStrategy.skipPastLastEvent())
                .followedBy("next")                // ⚠️ 用宽松连续（累计不要求严格连续）
                .timesOrMore(threshold - 1)        // start + (threshold-1) = threshold次
                .within(Time.minutes(minutes));

        PatternStream<Alarm> patternStream = CEP.pattern(keyedStream, pattern);

        return patternStream.process(
                new PatternProcessFunction<Alarm, AlarmLevel>() {

                    @Override
                    public void processMatch(
                            Map<String, List<Alarm>> match,
                            Context ctx,
                            Collector<AlarmLevel> out) {

                        Alarm first = match.get("start").get(0);
                        List<Alarm> nextList = match.get("next");

                        int count = 1 + (nextList == null ? 0 : nextList.size());

                        out.collect(new AlarmLevel(
                                first.getSyscode(),
                                first.getGroupcode(),
                                level,
                                count
                        ));
                    }
                }
        );
    }
}
