package com.atguigu.es.test.chat4;

public class EsRecord {
    private String channelCode;
    private String dctime;         // 时间（到分钟）
    private Double tps60;          // TPS
    private Double avgTime;        // 平均响应时间
    private Double sysSuccrate60;  // 成功率

    // getter/setter
    public String getChannelCode() { return channelCode; }
    public void setChannelCode(String channelCode) { this.channelCode = channelCode; }

    public String getDctime() { return dctime; }
    public void setDctime(String dctime) { this.dctime = dctime; }

    public Double getTps60() { return tps60; }
    public void setTps60(Double tps60) { this.tps60 = tps60; }

    public Double getAvgTime() { return avgTime; }
    public void setAvgTime(Double avgTime) { this.avgTime = avgTime; }

    public Double getSysSuccrate60() { return sysSuccrate60; }
    public void setSysSuccrate60(Double sysSuccrate60) { this.sysSuccrate60 = sysSuccrate60; }
}
