package com.abc.errorcode.process;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

public class AlarmProcessFunction
        extends KeyedProcessFunction<String, AlarmEvent, AlarmResult> {

    private ValueState<Integer> cntState;
    private ValueState<Long> timerState;

    @Override
    public void open(Configuration parameters) {
        cntState = getRuntimeContext().getState(
                new ValueStateDescriptor<>("cnt", Integer.class)
        );
        timerState = getRuntimeContext().getState(
                new ValueStateDescriptor<>("timer", Long.class)
        );
    }

    @Override
    public void processElement(
            AlarmEvent value,
            Context ctx,
            Collector<AlarmResult> out) throws Exception {

        Integer cnt = cntState.value();
        cnt = cnt == null ? 1 : cnt + 1;
        cntState.update(cnt);

        long now = ctx.timerService().currentProcessingTime();
        long triggerTime = now + Time.minutes(10).toMilliseconds();

        Long oldTimer = timerState.value();
        if (oldTimer == null || triggerTime > oldTimer) {
            if (oldTimer != null) {
                ctx.timerService().deleteProcessingTimeTimer(oldTimer);
            }
            ctx.timerService().registerProcessingTimeTimer(triggerTime);
            timerState.update(triggerTime);
        }

        if (cnt >= 3) {
            out.collect(new AlarmResult("ALARM", value.getKey()));
        }
    }

    @Override
    public void onTimer(long ts, OnTimerContext ctx, Collector<AlarmResult> out) {
        // 恢复 / 清理
        cntState.clear();
        timerState.clear();
        out.collect(new AlarmResult("RECOVER", ctx.getCurrentKey()));
    }

}

@Data
@AllArgsConstructor
@NoArgsConstructor
class AlarmEvent {
    String key;

}

@Data
@AllArgsConstructor
@NoArgsConstructor
class AlarmResult {
    String key;
    String value;

}
