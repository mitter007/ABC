package com.abc.watermark;


import com.abc.pojo.LogMetricDomain;
import org.apache.flink.api.common.eventtime.Watermark;
import org.apache.flink.api.common.eventtime.WatermarkGenerator;
import org.apache.flink.api.common.eventtime.WatermarkGeneratorSupplier;
import org.apache.flink.api.common.eventtime.WatermarkOutput;


public class WatermarkGenerator_New implements WatermarkGeneratorSupplier<LogMetricDomain> {

    private final long lateTime;

    public WatermarkGenerator_New(long lateTime) {
        this.lateTime = lateTime;
    }


    @Override
    public WatermarkGenerator<LogMetricDomain> createWatermarkGenerator(Context context) {
        return new WatermarkGenerator<LogMetricDomain>() {
            private long currentMaxTimestamp = Long.MIN_VALUE;

            @Override
            public void onEvent(LogMetricDomain event, long eventTimestamp, WatermarkOutput output) {
                // 提取时间戳，更新最大值
                long timestamp = event.getDate().getTime();
                currentMaxTimestamp = Math.max(timestamp, currentMaxTimestamp);
            }

            @Override
            public void onPeriodicEmit(WatermarkOutput output) {
                // 周期性发出 Watermark（默认200ms发一次）
                output.emitWatermark(new Watermark(currentMaxTimestamp - lateTime));
            }
        };
    }
}