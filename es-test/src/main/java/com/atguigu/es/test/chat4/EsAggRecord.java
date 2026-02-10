package com.atguigu.es.test.chat4;

public class EsAggRecord {
    private String channelCode;
    private String dctime;

    private Double minuteTotalTps;        // 每分钟总交易量
    private Double maxTps60;              // 每分钟 TPS 峰值
    private Double maxAvgTime;            // 每分钟响应峰值

    private Double avgTimeWeighted;       // 加权平均响应时间
    private Double sysSuccrateWeighted;   // 加权平均成功率

    // 临时存放加权和
    private Double weightedSumAvgTime;
    private Double weightedSumSysSuccrate;

    // getter/setter
    public String getChannelCode() { return channelCode; }
    public void setChannelCode(String channelCode) { this.channelCode = channelCode; }

    public String getDctime() { return dctime; }
    public void setDctime(String dctime) { this.dctime = dctime; }

    public Double getMinuteTotalTps() { return minuteTotalTps; }
    public void setMinuteTotalTps(Double minuteTotalTps) { this.minuteTotalTps = minuteTotalTps; }

    public Double getMaxTps60() { return maxTps60; }
    public void setMaxTps60(Double maxTps60) { this.maxTps60 = maxTps60; }

    public Double getMaxAvgTime() { return maxAvgTime; }
    public void setMaxAvgTime(Double maxAvgTime) { this.maxAvgTime = maxAvgTime; }

    public Double getAvgTimeWeighted() { return avgTimeWeighted; }
    public void setAvgTimeWeighted(Double avgTimeWeighted) { this.avgTimeWeighted = avgTimeWeighted; }

    public Double getSysSuccrateWeighted() { return sysSuccrateWeighted; }
    public void setSysSuccrateWeighted(Double sysSuccrateWeighted) { this.sysSuccrateWeighted = sysSuccrateWeighted; }

    public Double getWeightedSumAvgTime() { return weightedSumAvgTime; }
    public void setWeightedSumAvgTime(Double weightedSumAvgTime) { this.weightedSumAvgTime = weightedSumAvgTime; }

    public Double getWeightedSumSysSuccrate() { return weightedSumSysSuccrate; }
    public void setWeightedSumSysSuccrate(Double weightedSumSysSuccrate) { this.weightedSumSysSuccrate = weightedSumSysSuccrate; }
}
