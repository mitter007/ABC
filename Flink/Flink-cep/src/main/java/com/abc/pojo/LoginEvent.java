package com.abc.pojo;


import lombok.Data;

@Data
public class LoginEvent {

    public String userId;
    public String ip;
    public String eventType; // success / fail
    public long timestamp;

    public LoginEvent(){}

    public LoginEvent(String userId,String ip,String eventType,long timestamp){
        this.userId = userId;
        this.ip = ip;
        this.eventType = eventType;
        this.timestamp = timestamp;
    }
}