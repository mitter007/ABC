package com.abc.errorcode.upAlert.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Alarm {
    private String syscode;
    private String groupcode;
    private String firstDim;
    private String type;
    private long ts;

}