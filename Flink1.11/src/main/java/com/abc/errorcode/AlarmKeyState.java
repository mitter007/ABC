package com.abc.errorcode;

import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;

public class AlarmKeyState {
    public static final MapStateDescriptor<String, AlarmKey> ALARM_KEY_STATE =
        new MapStateDescriptor<>(
            "alarm_keys",
            Types.STRING,
            Types.POJO(AlarmKey.class)
        );
}
