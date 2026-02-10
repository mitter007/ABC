package com.abc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.*;
import org.apache.spark.streaming.kafka010.*;
import scala.Tuple2;

import java.util.*;
import java.util.concurrent.*;

public class App1 {

    private static volatile Broadcast<Map<String, RuleInfo>> rulesBroadcast = null;
    private static volatile Broadcast<Map<String, Set<String>>> historyBroadcast = null;

    private static final String RULES_API = "http://uops.uup.test.abc/rule";
    private static final String HISTORY_API = "http://uops.uup.test.abc/history/findall";

    public static void main(String[] args) throws Exception {
        SparkConf conf = new SparkConf().setAppName("SparkErrorCodeAlert").setMaster("local[*]");
        JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.minutes(5));

//        JavaSparkContext jsc = new JavaSparkContext(jssc.sparkContext());
        JavaSparkContext jsc = jssc.sparkContext();
        // 定时刷新规则
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            Map<String, RuleInfo> rules = RulesClient.fetchAllRules(RULES_API);
            if (rules != null) {
                synchronized (App1.class) {
                    if (rulesBroadcast != null) {
                        try { rulesBroadcast.unpersist(); } catch (Exception e) {}
                    }
                    rulesBroadcast = jsc.broadcast(rules);
                    System.out.println("规则刷新完成：" + new Date());
                }
            }
        }, 0, 5, TimeUnit.MINUTES);

        // 定时刷新历史数据（每天一次）
        scheduler.scheduleAtFixedRate(() -> {
            Map<String, Set<String>> hist = HistoryClient.fetchAllHistory(HISTORY_API);
            if (hist != null) {
                synchronized (App1.class) {
                    if (historyBroadcast != null) {
                        try { historyBroadcast.unpersist(); } catch (Exception e) {}
                    }
                    historyBroadcast = jsc.broadcast(hist);
                    System.out.println("历史数据刷新完成：" + new Date());
                }
            }
        }, 0, 24, TimeUnit.HOURS);

        // Kafka 配置
        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        kafkaParams.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        kafkaParams.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        kafkaParams.put(ConsumerConfig.GROUP_ID_CONFIG, "spark-alert");
        kafkaParams.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        kafkaParams.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        Collection<String> topics = Arrays.asList("uops_errorCode5");
        JavaInputDStream<org.apache.kafka.clients.consumer.ConsumerRecord<String, String>> stream =
                KafkaUtils.createDirectStream(
                        jssc,
                        LocationStrategies.PreferConsistent(),
                        ConsumerStrategies.Subscribe(topics, kafkaParams)
                );

        JavaDStream<String> lines = stream.map(r -> r.value());

        // 按 5 分钟窗口
        JavaDStream<String> windowed = lines.window(Durations.minutes(5));

        // transform 内只 map/filter/reduceByKey
        JavaDStream<AlertSender.AlertPayload> alerts = windowed.transform(rdd -> {
            final Broadcast<Map<String, RuleInfo>> bcRules = rulesBroadcast;
            final Broadcast<Map<String, Set<String>>> bcHistory = historyBroadcast;

            // 解析 + 规则过滤
            JavaRDD<AlertData> parsed = rdd.mapPartitions(iter -> {
                Map<String, RuleInfo> rules = bcRules == null ? new HashMap<>() : bcRules.value();
                List<AlertData> out = new ArrayList<>();
                while (iter.hasNext()) {
                    String line = iter.next();
                    JSONObject obj = JSON.parseObject(line);
                    String sys = obj.getString("syscode");
                    String grp = obj.getString("groupcode");
                    String scase = obj.getString("systemcase");
                    String firstDim = obj.getString("firstDim");
                    String secDim = obj.getString("secDim");
                    if (sys == null || grp == null || scase == null || firstDim == null) continue;

                    AlertData ad = new AlertData(sys, grp, scase, firstDim);
                    if (secDim != null && !secDim.isEmpty()) ad.addSecDim(secDim);

                    String ruleKey = sys + "|" + grp + "|" + scase;
                    RuleInfo ri = rules.get(ruleKey);
                    ad.setRuleInfo(ri);
                    if (ri != null && ri.getAlertSpec() != null && ri.getAlertSpec() == -1) continue;

                    out.add(ad);
                }
                return out.iterator();
            });

            // reduceByKey 聚合 secDim
            org.apache.spark.api.java.JavaPairRDD<String, AlertData> pairRDD = parsed.mapToPair(ad -> {
                String key = ad.getSyscode() + "|" + ad.getGroupcode() + "|" + ad.getSystemcase() + "|" + ad.getFirstDim();
                return new Tuple2<>(key, ad);
            });

            org.apache.spark.api.java.JavaPairRDD<String, AlertData> reduced = pairRDD.reduceByKey((a, b) -> {
                a.merge(b);
                return a;
            });

            // 对比历史数据识别新增错误码
            return reduced.flatMap(t -> {
                List<AlertSender.AlertPayload> outs = new ArrayList<>();
                AlertData data = t._2();
                String sys = data.getSyscode();
                String grp = data.getGroupcode();
                String scase = data.getSystemcase();
                String firstDim = data.getFirstDim();

                String histKey = sys + "|" + grp + "|" + scase;
                Map<String, Set<String>> hist = bcHistory == null ? new HashMap<>() : bcHistory.value();
                Set<String> histSet = hist.get(histKey);

                if (histSet == null || !histSet.contains(firstDim)) {
                    AlertSender.AlertPayload p = new AlertSender.AlertPayload(sys, grp, scase, firstDim, new ArrayList<>(data.getSecDims()));
                    p.setWindowTime(new Date());
                    outs.add(p);
                }
                return outs.iterator();
            });
        });

        // 发送告警
        alerts.foreachRDD(rdd -> rdd.foreach(AlertSender::sendAlert));

        jssc.start();
        jssc.awaitTermination();
    }
}
