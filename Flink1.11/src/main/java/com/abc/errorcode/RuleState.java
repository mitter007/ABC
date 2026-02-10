package com.abc.errorcode;

import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;

public class RuleState {
    public static final MapStateDescriptor<String, AlarmRule> RULE_STATE =
        new MapStateDescriptor<>(
            "alarm-rule",
            Types.STRING,
            Types.POJO(AlarmRule.class)
        );
}
