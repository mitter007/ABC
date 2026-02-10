package com.abc.errorcode;

import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.ReadOnlyBroadcastState;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.Collector;

public class DetailAlarmProcess
    extends KeyedBroadcastProcessFunction<
            String,        // key: sys|group|firstDim|window
            MetricEvent,   // 明细数据
            AlarmKey,      // 广播告警 Key
            MetricEvent    // 最终告警输出（或你自定义的 AlarmResult）
        > {

    @Override
    public void processElement(
            MetricEvent event,
            ReadOnlyContext ctx,
            Collector<MetricEvent> out) throws Exception {

        ReadOnlyBroadcastState<String, AlarmKey> alarmKeyState =
            ctx.getBroadcastState(AlarmKeyState.ALARM_KEY_STATE);

        String key =
            event.syscode + "|" +
            event.groupcode + "|" +
            event.firstDim + "|" +
            event.windowStart + "|" +
            event.windowEnd;

        if (alarmKeyState.contains(key)) {
            // 命中告警窗口 + 错误码
            out.collect(event);
        }
        if (1==1) {
            // 命中告警窗口 + 错误码
            out.collect(event);
        }
    }

    @Override
    public void processBroadcastElement(
            AlarmKey alarmKey,
            Context ctx,
            Collector<MetricEvent> out) throws Exception {

        BroadcastState<String, AlarmKey> state =
            ctx.getBroadcastState(AlarmKeyState.ALARM_KEY_STATE);

        state.put(alarmKey.key(), alarmKey);
    }
}
