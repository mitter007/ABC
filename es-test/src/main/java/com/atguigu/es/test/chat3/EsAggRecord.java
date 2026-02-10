package com.atguigu.es.test.chat3;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EsAggRecord {
    private String dctime;
    private Double totalTps60;
    private Double totalAvgTime;
    private Double totalSysSuccrate60;
    // 临时存放加权和
    private Double weightedSumAvgTime;
    private Double weightedSumSysSuccrate;
    // getter & setter

}
