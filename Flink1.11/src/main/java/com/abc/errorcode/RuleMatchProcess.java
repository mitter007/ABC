package com.abc.errorcode;

import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.ReadOnlyBroadcastState;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.Collector;

public class RuleMatchProcess
    extends KeyedBroadcastProcessFunction<
            String,          // key: syscode|groupcode
            MetricEvent,     // 主流
            AlarmRule,       // 广播规则
            AlarmKey         // 输出
        > {

    @Override
    public void processElement(
            MetricEvent event,
            ReadOnlyContext ctx,
            Collector<AlarmKey> out) throws Exception {

        ReadOnlyBroadcastState<String, AlarmRule> ruleState =
            ctx.getBroadcastState(RuleState.RULE_STATE);

        String ruleKey = event.syscode + "|" + event.groupcode;
        AlarmRule rule = ruleState.get(ruleKey);

        if (rule == null) {
            return;
        }

        // 示例规则判断（你可以替换成真实逻辑）
        if (event.tps >= rule.threshold) {
            AlarmKey alarmKey = new AlarmKey();
            alarmKey.syscode = event.syscode;
            alarmKey.groupcode = event.groupcode;
            alarmKey.firstDim = event.firstDim;
            alarmKey.windowStart = event.windowStart;
            alarmKey.windowEnd = event.windowEnd;

            out.collect(alarmKey);
        }
    }

    @Override
    public void processBroadcastElement(
            AlarmRule rule,
            Context ctx,
            Collector<AlarmKey> out) throws Exception {

        BroadcastState<String, AlarmRule> state =
            ctx.getBroadcastState(RuleState.RULE_STATE);

        String key = rule.syscode + "|" + rule.groupcode;
        state.put(key, rule);
    }
}
