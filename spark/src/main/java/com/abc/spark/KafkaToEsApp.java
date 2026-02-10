package com.abc.spark;

import com.alibaba.fastjson.JSON;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.elasticsearch.spark.streaming.api.java.JavaEsSparkStreaming;

import java.util.*;

public class KafkaToEsApp {

    public static void main(String[] args) throws Exception {

        SparkConf sparkConf = new SparkConf()
                .setAppName("KafkaToES")
                .set("es.nodes", "your-es-host")
                .set("es.port", "9200");

        JavaStreamingContext jssc =
                new JavaStreamingContext(sparkConf, Durations.minutes(1));

        Map<String, Object> kafkaPara = new HashMap<>();
        kafkaPara.put("bootstrap.servers", "yourKafkaBroker:9092");
        kafkaPara.put("key.deserializer", StringDeserializer.class);
        kafkaPara.put("value.deserializer", StringDeserializer.class);
        kafkaPara.put("group.id", "group_test");
        kafkaPara.put("auto.offset.reset", "latest");
        kafkaPara.put("enable.auto.commit", false);

        final JavaInputDStream<ConsumerRecord<String, String>> stream =
                KafkaUtils.createDirectStream(
                        jssc,
                        LocationStrategies.PreferConsistent(),
                        ConsumerStrategies.Subscribe(
                                Collections.singleton("your_topic"),
                                kafkaPara
                        )
                );

        JavaDStream<String> aggregatedStream =
                stream.mapPartitions(new FlatMapFunction<Iterator<ConsumerRecord<String, String>>, String>() {
                    @Override
                    public Iterator<String> call(Iterator<ConsumerRecord<String, String>> iter) throws Exception {

                        List<Map<String, Object>> allList = new ArrayList<>();

                        while (iter.hasNext()) {
                            String value = iter.next().value();
                            if (value == null || value.length() == 0) {
                                continue;
                            }
                            Map<String, Object> map = JSON.parseObject(value, Map.class);
                            allList.add(map);
                        }

                        // key: syscode|groupcode|firstDim|kpidate
                        Map<String, List<Map<String, Object>>> grouped = new HashMap<>();

                        for (int i = 0; i < allList.size(); i++) {
                            Map<String, Object> m = allList.get(i);

                            String syscode = (String) m.get("syscode");
                            String groupcode = (String) m.get("groupcode");
                            String firstDim = (String) m.get("firstDim");
                            String kpidate = (String) m.get("kpidate");

                            String key = syscode + "|" + groupcode + "|" + firstDim + "|" + kpidate;

                            List<Map<String, Object>> list = grouped.get(key);
                            if (list == null) {
                                list = new ArrayList<Map<String, Object>>();
                                grouped.put(key, list);
                            }
                            list.add(m);
                        }

                        List<String> output = new ArrayList<>();

                        // 遍历每个 key 颗粒度
                        Iterator<Map.Entry<String, List<Map<String, Object>>>> it = grouped.entrySet().iterator();
                        while (it.hasNext()) {

                            Map.Entry<String, List<Map<String, Object>>> entry = it.next();
                            List<Map<String, Object>> oneGroup = entry.getValue();

                            // 1. 先找 secDim = TOTAL 的
                            Map<String, Object> totalObj = null;

                            for (int i = 0; i < oneGroup.size(); i++) {
                                Map<String, Object> m = oneGroup.get(i);
                                String secDim = (String) m.get("secDim");
                                if ("TOTAL".equals(secDim)) {
                                    totalObj = m;
                                    break;
                                }
                            }

                            // 2. 如果存在 TOTAL → 直接输出
                            if (totalObj != null) {
                                output.add(JSON.toJSONString(totalObj));
                                continue;
                            }

                            // 3. 没有 TOTAL → 手工聚合
                            int totalTps = 0;
                            Map<String, Object> base = new HashMap<String, Object>();

                            // 取第一个作为基础字段
                            Map<String, Object> first = oneGroup.get(0);

                            base.put("syscode", first.get("syscode"));
                            base.put("groupcode", first.get("groupcode"));
                            base.put("firstDim", first.get("firstDim"));
                            base.put("kpidate", first.get("kpidate"));
                            base.put("systemcase", first.get("systemcase"));
                            base.put("windowStart", first.get("windowStart"));
                            base.put("windowEnd", first.get("windowEnd"));
                            base.put("windowSize", first.get("windowSize"));
                            base.put("metricTimestamp", first.get("metricTimestamp"));
                            base.put("aggTime", first.get("aggTime"));

                            base.put("secDim", "TOTAL");

                            // tps 求和
                            for (int i = 0; i < oneGroup.size(); i++) {
                                Map<String, Object> m = oneGroup.get(i);

                                Object tpsObj = m.get("tps");
                                if (tpsObj != null) {
                                    int tps = Integer.parseInt(tpsObj.toString());
                                    totalTps += tps;
                                }
                            }

                            base.put("tps", totalTps);

                            output.add(JSON.toJSONString(base));
                        }

                        return output.iterator();
                    }
                });

        HashMap<String, String> esConf = new HashMap<String, String>();
        esConf.put("es.resource.write", "uops_index/kpidate");
        esConf.put("es.input.json", "yes");

        JavaEsSparkStreaming.saveJsonToEs(aggregatedStream, esConf);

        jssc.start();
        jssc.awaitTermination();
    }
}
