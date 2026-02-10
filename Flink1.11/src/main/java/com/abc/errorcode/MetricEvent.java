package com.abc.errorcode;

// Kafka 中的指标事件
public class MetricEvent {
    public String syscode;
    public String groupcode;
    public String firstDim;   // 错误码 / TOTAL
    public String secDim;     // 明细维度 / TOTAL
    public long windowStart;
    public long windowEnd;
    public long tps;
}
