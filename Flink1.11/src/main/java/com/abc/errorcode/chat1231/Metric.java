package com.abc.errorcode.chat1231;

public class Metric {

    public String sys;
    public String group;
    public String firstDim;
    public String secDim;     // total / detail
    public long windowEnd;    // 上游事件时间窗口结束
    public double value;

    // 必须要无参构造
    public Metric() {}
}
