package com.abc.cep.job;

import com.abc.pojo.LoginEvent;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.cep.*;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.util.List;
import java.util.Map;

public class CepJob_01 {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStream<LoginEvent> stream = env
                .fromElements(
                        new LoginEvent("user1", "192.168.1.1", "fail", 1000),
                        new LoginEvent("user1", "192.168.1.1", "fail", 2000),
                        new LoginEvent("user1", "192.168.1.1", "fail", 3000)
                )
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy
                                .<LoginEvent>forMonotonousTimestamps()
                                .withTimestampAssigner((event, t) -> event.timestamp)
                );
        KeyedStream<LoginEvent, String> keyedStream =
                stream.keyBy(e -> e.userId);
        Pattern<LoginEvent, ?> pattern =
                Pattern.<LoginEvent>begin("fail")
                        .where(new IterativeCondition<LoginEvent>() {
                            @Override
                            public boolean filter(LoginEvent e, Context<LoginEvent> context) throws Exception {
                                return e.getEventType().equals("fail");
                            }
                        })
                        .times(3)
                        .consecutive()
                        .within(Time.seconds(5));
        PatternStream<LoginEvent> patternStream =
                CEP.pattern(keyedStream, pattern);

//        七、处理匹配结果
        SingleOutputStreamOperator<String> result =
                patternStream.select(
                        new PatternSelectFunction<LoginEvent, String>() {
                            @Override
                            public String select(Map<String, List<LoginEvent>> m) throws Exception {
                                List<LoginEvent> fails =
                                        m.get("fail");

                                LoginEvent first = fails.get(0);

                                return "ALERT: 用户 "
                                        + first.userId
                                        + " 连续3次登录失败";
                            }
                        }
                );


        OutputTag<String> timeoutTag =
                new OutputTag<String>("timeout") {
                };

//        八、生产级必备：超时处理
        SingleOutputStreamOperator<String> result1 =
                patternStream.flatSelect(
                        timeoutTag,
                        new PatternFlatTimeoutFunction<LoginEvent, String>() {
                            @Override
                            public void timeout(
                                    Map<String, List<LoginEvent>> pattern,
                                    long timeoutTimestamp,
                                    Collector<String> out) {

                                out.collect("登录失败序列超时");
                            }
                        },
                        new PatternFlatSelectFunction<LoginEvent, String>() {
                            @Override
                            public void flatSelect(
                                    Map<String, List<LoginEvent>> pattern,
                                    Collector<String> out) {

                                out.collect("触发风控");
                            }
                        }
                );


        DataStream<String> timeoutStream =
                result.getSideOutput(timeoutTag);


//        Kafka
//  ↓
//        Source
//  ↓
//        Watermark
//  ↓
//        keyBy(userId)
//  ↓
//        CEP Pattern
//  ↓
//        PatternSelect
//  ↓
//        报警系统

        env.execute("Cep_Job-01");


    }
}