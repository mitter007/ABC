package com.atguigu.es.test.chat3;

import java.util.*;
import java.util.stream.Collectors;

public class EsAggregator1 {

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

            agg.setTotalTps60(
                    (agg.getTotalTps60() == null ? 0.0 : agg.getTotalTps60())
                            + (r.getTps60() == null ? 0.0 : r.getTps60())
            );
            agg.setTotalAvgTime(
                    (agg.getTotalAvgTime() == null ? 0.0 : agg.getTotalAvgTime())
                            + (r.getAvgTime() == null ? 0.0 : r.getAvgTime())
            );
            agg.setTotalSysSuccrate60(
                    (agg.getTotalSysSuccrate60() == null ? 0.0 : agg.getTotalSysSuccrate60())
                            + (r.getSysSuccrate60() == null ? 0.0 : r.getSysSuccrate60())
            );

            aggMap.put(dctime, agg);
        }

        List<EsAggRecord> aggList = new ArrayList<>(aggMap.values());

        // 3. 分别取指标的 Top3
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

        // 4. 输出结果
        System.out.println("=== Top 3 tps60 ===");
        top3Tps.forEach(r -> System.out.println(r.getDctime() + " : " + r.getTotalTps60()));

        System.out.println("=== Top 3 avgTime ===");
        top3Avg.forEach(r -> System.out.println(r.getDctime() + " : " + r.getTotalAvgTime()));

        System.out.println("=== Top 3 sysSuccrate60 ===");
        top3Succ.forEach(r -> System.out.println(r.getDctime() + " : " + r.getTotalSysSuccrate60()));
    }
}
