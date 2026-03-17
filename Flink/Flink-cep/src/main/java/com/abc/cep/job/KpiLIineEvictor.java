package com.abc.cep.job;

import org.apache.flink.streaming.api.windowing.evictors.Evictor;
import org.apache.flink.streaming.api.windowing.windows.Window;
import org.apache.flink.streaming.runtime.operators.windowing.TimestampedValue;

import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/**
 * ClassName: KpiLIineEvictor
 * Package: com.abc.cep.job
 * Description:
 *
 * @Author JWT
 * @Create 2026/3/13 15:58
 * @Version 1.0
 */
public class KpiLIineEvictor<W extends Window> implements Evictor<HashMap<String, Object>, W> {
    private final SimpleDateFormat sim = new SimpleDateFormat("yyyyMMddHHmmss");
    private final int windowSize;
    private final int delyaime;

    public KpiLIineEvictor(int windowSize, int delyaime) {
        this.windowSize = windowSize;
        this.delyaime = delyaime;
    }


    @Override
    public void evictBefore(Iterable<TimestampedValue<HashMap<String, Object>>> iterable, int size, W window, EvictorContext evictorContext) {
        long maxTimeLimit = window.maxTimestamp();
        int i = 1000;
        long startTime = maxTimeLimit - windowSize * i;
        for (Iterator<TimestampedValue<HashMap<String, Object>>> iterator = iterable.iterator(); iterator.hasNext(); ) {
            TimestampedValue<HashMap<String, Object>> record = iterator.next();
            HashMap<String, Object> item = record.getValue();
            Date itemDate = null;

            try {
                itemDate = sim.parse(item.get("dctime").toString());

                if (itemDate != null && itemDate.getTime() > startTime && itemDate.getTime() <= (maxTimeLimit - delyaime * 1000)) {

                } else {
                    iterator.remove();
                }

            } catch (ParseException e) {
                throw new RuntimeException(e);
            }


        }

    }

    @Override
    public void evictAfter(Iterable<TimestampedValue<HashMap<String, Object>>> elements, int size, W window, EvictorContext evictorContext) {

    }


}
