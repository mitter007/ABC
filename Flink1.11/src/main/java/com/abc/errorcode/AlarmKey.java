package com.abc.errorcode;

// 阶段 1 输出的告警 Key
public class AlarmKey {
    public String syscode;
    public String groupcode;
    public String firstDim;
    public long windowStart;
    public long windowEnd;

    // key 串，便于 broadcast / state 使用
    public String key() {
        return syscode + "|" + groupcode + "|" + firstDim + "|" + windowStart + "|" + windowEnd;
    }
}
