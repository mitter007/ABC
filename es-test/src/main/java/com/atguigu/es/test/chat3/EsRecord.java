package com.atguigu.es.test.chat3;

public class EsRecord {
    private String syscode;
    private String groupcode;
    private String dimcont;
    private String dctime;
    private Double tps60;
    private Double avgTime;
    private Double sysSuccrate60;

    // getter & setter
    public String getSyscode() { return syscode; }
    public void setSyscode(String syscode) { this.syscode = syscode; }

    public String getGroupcode() { return groupcode; }
    public void setGroupcode(String groupcode) { this.groupcode = groupcode; }

    public String getDimcont() { return dimcont; }
    public void setDimcont(String dimcont) { this.dimcont = dimcont; }

    public String getDctime() { return dctime; }
    public void setDctime(String dctime) { this.dctime = dctime; }

    public Double getTps60() { return tps60; }
    public void setTps60(Double tps60) { this.tps60 = tps60; }

    public Double getAvgTime() { return avgTime; }
    public void setAvgTime(Double avgTime) { this.avgTime = avgTime; }

    public Double getSysSuccrate60() { return sysSuccrate60; }
    public void setSysSuccrate60(Double sysSuccrate60) { this.sysSuccrate60 = sysSuccrate60; }

    @Override
    public String toString() {
        return "EsRecord{" +
                "syscode='" + syscode + '\'' +
                ", groupcode='" + groupcode + '\'' +
                ", dimcont='" + dimcont + '\'' +
                ", dctime='" + dctime + '\'' +
                ", tps60=" + tps60 +
                ", avgTime=" + avgTime +
                ", sysSuccrate60=" + sysSuccrate60 +
                '}';
    }
}
