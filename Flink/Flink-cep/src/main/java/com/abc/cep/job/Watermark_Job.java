package com.abc.cep.job;

import com.abc.pojo.LoginEvent;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkGenerator;
import org.apache.flink.api.common.eventtime.WatermarkOutput;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.functions.AssignerWithPunctuatedWatermarks;
import org.apache.flink.streaming.api.watermark.Watermark;

import javax.annotation.Nullable;
import java.time.Duration;

public class Watermark_Job {
    public static void main(String[] args) throws Exception {
       final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<LoginEvent> stream = env
                .fromElements(
                        new LoginEvent("user2", "192.168.1.1", "fail", 1000),
                        new LoginEvent("user1", "192.168.1.1", "fail", 2000),
                        new LoginEvent("user1", "192.168.1.1", "fail", 3000)
                );
        stream.assignTimestampsAndWatermarks(WatermarkStrategy.<LoginEvent>forBoundedOutOfOrderness(Duration.ofSeconds(15)).withTimestampAssigner(
                new SerializableTimestampAssigner<LoginEvent>() {
                    @Override
                    public long extractTimestamp(LoginEvent element, long recordTimestamp) {
                        return element.getTimestamp();
                    }
                }
        ));

        // ❌ 这个过时了（旧版，传入 AssignerWithPeriodicWatermarks）
        stream.assignTimestampsAndWatermarks(new AssignerWithPeriodicWatermarks<LoginEvent>() {
            @Nullable
            @Override
            public Watermark getCurrentWatermark() {
                return null;
            }

            @Override
            public long extractTimestamp(LoginEvent element, long recordTimestamp) {
                return 0;
            }
        });

// ❌ 这个也过时了（旧版，传入 AssignerWithPunctuatedWatermarks）
        stream.assignTimestampsAndWatermarks(new AssignerWithPunctuatedWatermarks<LoginEvent>() {
            @Nullable
            @Override
            public Watermark checkAndGetNextWatermark(LoginEvent lastElement, long extractedTimestamp) {
                return null;
            }

            @Override
            public long extractTimestamp(LoginEvent element, long recordTimestamp) {
                return 0;
            }
        });

// 场景1：数据完全有序，无乱序
        WatermarkStrategy.<LoginEvent>forMonotonousTimestamps()
                .withTimestampAssigner((e, ts) -> e.timestamp);

// 场景2：允许乱序（最常用，对应旧版 BoundedOutOfOrdernessTimestampExtractor）
        WatermarkStrategy.<LoginEvent>forBoundedOutOfOrderness(Duration.ofSeconds(5))
                .withTimestampAssigner((e, ts) -> e.timestamp);

// 场景3：自定义复杂逻辑（对应旧版完全自定义实现）
        WatermarkStrategy.<LoginEvent>forGenerator(ctx -> new WatermarkGenerator<LoginEvent>() {
                    private long maxTimestamp = Long.MIN_VALUE;

                    @Override
                    public void onEvent(LoginEvent event, long eventTimestamp, WatermarkOutput output) {
                        maxTimestamp = Math.max(maxTimestamp, event.timestamp);
                    }

                    @Override
                    public void onPeriodicEmit(WatermarkOutput output) {
                        output.emitWatermark(new org.apache.flink.api.common.eventtime.Watermark(maxTimestamp - 5000));
                    }
                })
                .withTimestampAssigner((e, ts) -> e.timestamp);


        env.execute("Cep_Job-01");


    }
}