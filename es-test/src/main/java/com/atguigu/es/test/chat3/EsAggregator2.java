package com.atguigu.es.test.chat3;

import java.util.*;
import java.util.stream.Collectors;

public class EsAggregator2 {

    public static void aggregateTop3(List<List<EsRecord>> multiResultLists) {
        // 1. 合并所有 List<EsRecord>
        List<EsRecord> allRecords = multiResultLists.stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        // 2. 按 dctime 分组并聚合
        Map<String, EsAggRecord> aggMap = new HashMap<>();
        for (EsRecord r : allRecords) {
            String dctime = r.getDctime();
            EsAggRecord agg = aggMap.getOrDefault(dctime, new EsAggRecord());
            agg.setDctime(dctime);

            double tps = (r.getTps60() == null ? 0.0 : r.getTps60());
            double avg = (r.getAvgTime() == null ? 0.0 : r.getAvgTime());
            double succ = (r.getSysSuccrate60() == null ? 0.0 : r.getSysSuccrate60());

            // 总 TPS
            agg.setTotalTps60(
                    (agg.getTotalTps60() == null ? 0.0 : agg.getTotalTps60()) + tps
            );

            // 累加 tps * avgTime
            agg.setWeightedSumAvgTime(
                    (agg.getWeightedSumAvgTime() == null ? 0.0 : agg.getWeightedSumAvgTime()) + tps * avg
            );

            // 累加 tps * sysSuccrate60
            agg.setWeightedSumSysSuccrate(
                    (agg.getWeightedSumSysSuccrate() == null ? 0.0 : agg.getWeightedSumSysSuccrate()) + tps * succ
            );

            aggMap.put(dctime, agg);
        }

        // 3. 计算加权平均
        List<EsAggRecord> aggList = new ArrayList<>(aggMap.values());
        for (EsAggRecord agg : aggList) {
            double totalTps = agg.getTotalTps60() == null ? 0.0 : agg.getTotalTps60();

            if (totalTps > 0) {
                agg.setTotalAvgTime(agg.getWeightedSumAvgTime() / totalTps);
                agg.setTotalSysSuccrate60(agg.getWeightedSumSysSuccrate() / totalTps);
            } else {
                agg.setTotalAvgTime(0.0);
                agg.setTotalSysSuccrate60(0.0);
            }
        }

        // 4. 分别取指标的 Top3
        List<EsAggRecord> top3Tps = aggList.stream()
                .sorted(Comparator.comparingDouble(EsAggRecord::getTotalTps60).reversed())
                .limit(3)
                .collect(Collectors.toList());

        List<EsAggRecord> top3Avg = aggList.stream()
                .sorted(Comparator.comparingDouble(EsAggRecord::getTotalAvgTime).reversed())
                .limit(3)
                .collect(Collectors.toList());

        List<EsAggRecord> top3Succ = aggList.stream()
                .sorted(Comparator.comparingDouble(EsAggRecord::getTotalSysSuccrate60).reversed())
                .limit(3)
                .collect(Collectors.toList());

        // 5. 输出结果
        System.out.println("=== Top 3 tps60 ===");
        top3Tps.forEach(r -> System.out.println(r.getDctime() + " : " + r.getTotalTps60()));

        System.out.println("=== Top 3 avgTime (weighted) ===");
        top3Avg.forEach(r -> System.out.println(r.getDctime() + " : " + r.getTotalAvgTime()));

        System.out.println("=== Top 3 sysSuccrate60 (weighted) ===");
        top3Succ.forEach(r -> System.out.println(r.getDctime() + " : " + r.getTotalSysSuccrate60()));
    }
}
