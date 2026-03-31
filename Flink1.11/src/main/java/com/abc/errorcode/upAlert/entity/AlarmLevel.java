package com.abc.errorcode.upAlert.entity;

public class AlarmLevel {

    private String syscode;
    private String groupcode;
    private int level;
    private long duration;

    public AlarmLevel(String syscode, String groupcode, int level, long duration) {
        this.syscode = syscode;
        this.groupcode = groupcode;
        this.level = level;
        this.duration = duration;
    }
}