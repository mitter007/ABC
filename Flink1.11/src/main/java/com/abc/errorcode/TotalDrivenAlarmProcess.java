package com.abc.errorcode;

import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.List;

public class TotalDrivenAlarmProcess
    extends KeyedBroadcastProcessFunction<String, MetricEvent, AlarmRule, AlarmResult> {

    // TOTAL 是否命中规则
    private ValueState<Boolean> totalHitState;

    // 明细 secDim 缓存
    private ListState<SecDimDetail> detailState;

    @Override
    public void open(Configuration parameters) {

        totalHitState = getRuntimeContext().getState(
            new ValueStateDescriptor<>("totalHit", Types.BOOLEAN)
        );

        detailState = getRuntimeContext().getListState(
            new ListStateDescriptor<>(
                "secDimDetails",
                Types.POJO(SecDimDetail.class)
            )
        );
    }

    @Override
    public void processBroadcastElement(
            AlarmRule rule,
            Context ctx,
            Collector<AlarmResult> out) throws Exception {

        String key = rule.syscode + "|" + rule.groupcode + "|" + rule.firstDim;
        ctx.getBroadcastState(RuleState.RULE_STATE).put(key, rule);
    }

    @Override
    public void processElement(
            MetricEvent value,
            ReadOnlyContext ctx,
            Collector<AlarmResult> out) throws Exception {

        // 注册统一 window 触发时间
        ctx.timerService().registerEventTimeTimer(value.windowEnd + 1);
        ctx.timerService().currentProcessingTime();
        ctx.timerService().currentWatermark();
        ctx.timestamp();

        String ruleKey =
            value.syscode + "|" + value.groupcode + "|" + value.firstDim;

        AlarmRule rule =
            ctx.getBroadcastState(RuleState.RULE_STATE).get(ruleKey);

        if (rule == null) {
            return;
        }

        // ① TOTAL：只负责决策
        if ("TOTAL".equals(value.secDim)) {
            if (value.tps >= rule.threshold) {
                totalHitState.update(true);
            }
            return;
        }

        // ② 明细：只缓存，不判断规则
        SecDimDetail detail = new SecDimDetail();
        detail.secDim = value.secDim;
        detail.tps = value.tps;
        detailState.add(detail);
    }

    @Override
    public void onTimer(
            long timestamp,
            OnTimerContext ctx,
            Collector<AlarmResult> out) throws Exception {

        Boolean totalHit = totalHitState.value();

        // TOTAL 未命中 → 不输出
        if (totalHit == null || !totalHit) {
            clearState();
            return;
        }

        List<SecDimDetail> details = new ArrayList<>();
        for (SecDimDetail d : detailState.get()) {
            details.add(d);
        }

        if (details.isEmpty()) {
            clearState();
            return;
        }

        String[] parts = ctx.getCurrentKey().split("\\|");

        AlarmResult result = new AlarmResult();
        result.syscode = parts[0];
        result.groupcode = parts[1];
        result.firstDim = parts[2];
        result.windowStart = Long.parseLong(parts[3]);
        result.windowEnd = Long.parseLong(parts[4]);
        result.secDimDetails = details;

        out.collect(result);
        clearState();
    }

    private void clearState() throws Exception {
        totalHitState.clear();
        detailState.clear();
    }
}
