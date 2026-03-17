package com.abc.cep.job;


import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.functions.PatternProcessFunction;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

import java.util.List;
import java.util.Map;

public class CepJob_02 {

    // 登录事件
    public static class LoginEvent {
        public String userId;
        public String ip;
        public String status; // "success" or "fail"
        public long timestamp;

        public LoginEvent(String userId, String ip, String status, long timestamp) {
            this.userId = userId;
            this.ip = ip;
            this.status = status;
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return "LoginEvent{userId=" + userId + ", ip=" + ip + 
                   ", status=" + status + ", ts=" + timestamp + "}";
        }
    }

    // 告警事件
    public static class Alert {
        public String userId;
        public String message;
        public long triggerTime;

        public Alert(String userId, String message, long triggerTime) {
            this.userId = userId;
            this.message = message;
            this.triggerTime = triggerTime;
        }

        @Override
        public String toString() {
            return "🚨 Alert{userId=" + userId + ", msg=" + message + 
                   ", time=" + triggerTime + "}";
        }
    }

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // ① 模拟登录事件流（实际生产用 Kafka source）
        DataStream<LoginEvent> loginStream = env.fromElements(
            new LoginEvent("user_001", "192.168.1.1", "fail",  1000L),
            new LoginEvent("user_001", "192.168.1.1", "fail",  2000L),
            new LoginEvent("user_002", "10.0.0.1",    "fail",  3000L), // 干扰：其他用户
            new LoginEvent("user_001", "192.168.1.1", "fail",  4000L), // ← 触发！
            new LoginEvent("user_001", "192.168.1.1", "success", 5000L),
            new LoginEvent("user_003", "172.16.0.1",  "fail",  6000L),
            new LoginEvent("user_003", "172.16.0.1",  "fail",  7000L),
            new LoginEvent("user_003", "172.16.0.1",  "fail",  8000L)  // ← 触发！
        ).assignTimestampsAndWatermarks(
            // 使用事件时间
            WatermarkStrategy
                .<LoginEvent>forMonotonousTimestamps()
                .withTimestampAssigner((event, ts) -> event.timestamp)
        );

        // ② 定义 CEP Pattern：5分钟内连续3次登录失败
        Pattern<LoginEvent, ?> pattern = Pattern.<LoginEvent>begin("first_fail")
            .where(new SimpleCondition<LoginEvent>() {
                @Override
                public boolean filter(LoginEvent event) {
                    return "fail".equals(event.status);
                }
            })
            .next("second_fail")   // 宽松：中间可以有其他事件用 followedBy
            .where(new SimpleCondition<LoginEvent>() {
                @Override
                public boolean filter(LoginEvent event) {
                    return "fail".equals(event.status);
                }
            })
            .next("third_fail")
            .where(new SimpleCondition<LoginEvent>() {
                @Override
                public boolean filter(LoginEvent event) {
                    return "fail".equals(event.status);
                }
            })
            .within(Time.minutes(5));  // ← 核心：5分钟时间窗口

        // ③ 按用户 ID 分组，再应用 Pattern（重要！每个用户独立检测）
        PatternStream<LoginEvent> patternStream = CEP.pattern(
            loginStream.keyBy(e -> e.userId),
            pattern
        );

        // ④ 处理匹配结果
        DataStream<Alert> alerts = patternStream.process(
            new PatternProcessFunction<LoginEvent, Alert>() {
                @Override
                public void processMatch(
                    Map<String, List<LoginEvent>> match,
                    Context ctx,
                    Collector<Alert> out) {

                    LoginEvent first = match.get("first_fail").get(0);
                    LoginEvent third = match.get("third_fail").get(0);

                    out.collect(new Alert(
                        first.userId,
                        "5分钟内登录失败3次！IP: " + third.ip,
                        ctx.timestamp()
                    ));
                }
            }
        );

        // ⑤ 输出（实际生产写 Kafka / Redis / 数据库）
        alerts.print();

        env.execute("Login Fail CEP Demo");
    }
}