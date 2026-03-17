package com.abc.watermark;

import com.abc.pojo.LogMetricDomain;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.watermark.Watermark;

import javax.annotation.Nullable;
import java.util.Date;

/**
 * ClassName: WatermarkGeneratr
 * Package: com.abc.cep.job.watermark
 * Description:
 *
 * @Author JWT
 * @Create 2026/3/13 15:16
 * @Version 1.0
 */
public class WatermarkGenerator_old implements AssignerWithPeriodicWatermarks<LogMetricDomain> {
    private long currentMaxTimestamp;
    private final long lateTime;

    public WatermarkGenerator_old(long lateTime) {
        this.lateTime = lateTime;
    }

    @Override
    public long extractTimestamp(LogMetricDomain e, long recordTimestamp) {
        Date date = e.getDate();
        long timestamp = date.getTime();
        currentMaxTimestamp = Math.max(timestamp, currentMaxTimestamp);
        return timestamp;
    }

    @Nullable
    @Override
    public Watermark getCurrentWatermark() {
        return new Watermark(currentMaxTimestamp - lateTime);


    }
}
