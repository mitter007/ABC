package com.abc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.*;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.Time;
import org.apache.spark.streaming.api.java.*;
import org.apache.spark.streaming.kafka010.*;
import scala.Tuple2;

import java.util.*;
import java.util.concurrent.*;

/**
 * App - 优化版
 *
 * - rule/filter 放在 window 之前
 * - rules 每 5 分钟更新并 broadcast
 * - history 每天更新并 broadcast
 * - reduceByKey 聚合 secDim
 * - Driver 端按 (key + windowStart) 去重保证同一窗口只告警一次
 *
 * 注意：保持匿名类实现（无 lambda）
 */
public class App {

    private static volatile Broadcast<Map<String, RuleInfo>> rulesBroadcast = null;
    private static volatile Broadcast<Map<String, Set<String>>> historyBroadcast = null;

    private static final String RULES_API = "http://uops.uup.test.abc/rule";
    private static final String HISTORY_API = "http://uops.uup.test.abc/history/findall";

    // Driver 级别的去重缓存（存储 key|windowStartMillis）
    private static final Set<String> alertCache = ConcurrentHashMap.newKeySet();

    // 定时清理 alertCache 的线程（避免内存无限增长）
    static {
        ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();
        cleaner.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                alertCache.clear();
                System.out.println("[" + new Date() + "] alertCache cleared.");
            }
        }, 20, 20, TimeUnit.MINUTES);
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Usage: App <kafka-bootstrap-servers> <groupId> <topic>");
            System.exit(1);
        }
        final String bootstrapServers = args[0];
        final String kafkaGroup = args[1];
        final String topic = args[2];

        SparkConf conf = new SparkConf().setAppName("SparkErrorCodeAlert");
        if (!conf.contains("spark.master")) {
            conf.setMaster("local[*]");
        }

        // micro-batch interval 60s (更细粒度), window 5min slide 5min（非重叠）
        final JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(60));
        final JavaSparkContext jsc = jssc.sparkContext();

        // -------- schedule rules refresh every 5 minutes --------
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<String, RuleInfo> rules = RulesClient.fetchAllRules(RULES_API);
                    if (rules != null) {
                        synchronized (App.class) {
                            if (rulesBroadcast != null) {
                                try { rulesBroadcast.unpersist(); } catch (Exception e) {}
                            }
                            rulesBroadcast = jsc.broadcast(rules);
                            System.out.println("[" + new Date() + "] rules refreshed, size=" + rules.size());
                        }
                    } else {
                        System.out.println("[" + new Date() + "] rules fetch returned null");
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }, 0, 5, TimeUnit.MINUTES);

        // -------- schedule history refresh once per day --------
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<String, Set<String>> hist = HistoryClient.fetchAllHistory(HISTORY_API);
                    if (hist != null) {
                        synchronized (App.class) {
                            if (historyBroadcast != null) {
                                try { historyBroadcast.unpersist(); } catch (Exception e) {}
                            }
                            historyBroadcast = jsc.broadcast(hist);
                            System.out.println("[" + new Date() + "] history refreshed, size=" + hist.size());
                        }
                    } else {
                        System.out.println("[" + new Date() + "] history fetch returned null");
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }, 0, 24, TimeUnit.HOURS);

        // -------- Kafka params and stream --------
        Map<String, Object> kafkaParams = new HashMap<String, Object>();
        kafkaParams.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        kafkaParams.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        kafkaParams.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        kafkaParams.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaGroup);
        kafkaParams.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        kafkaParams.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        Collection<String> topics = new ArrayList<String>();
        topics.add(topic);

        final JavaInputDStream<org.apache.kafka.clients.consumer.ConsumerRecord<String, String>> stream =
                KafkaUtils.createDirectStream(
                        jssc,
                        LocationStrategies.PreferConsistent(),
                        ConsumerStrategies.<String, String>Subscribe(topics, kafkaParams)
                );

        // parse lines
        JavaDStream<String> lines = stream.map(new Function<org.apache.kafka.clients.consumer.ConsumerRecord<String, String>, String>() {
            @Override
            public String call(org.apache.kafka.clients.consumer.ConsumerRecord<String, String> record) throws Exception {
                return record.value();
            }
        });

        // -------- pre-window filtering: parse + rules + history  (performed per micro-batch) --------
        JavaDStream<AlertData> filtered = lines.transform(new Function<JavaRDD<String>, JavaRDD<AlertData>>() {
            @Override
            public JavaRDD<AlertData> call(JavaRDD<String> rdd) throws Exception {
                final Broadcast<Map<String, RuleInfo>> bcRules = rulesBroadcast;
                final Broadcast<Map<String, Set<String>>> bcHistory = historyBroadcast;

                return rdd.mapPartitions(new FlatMapFunction<Iterator<String>, AlertData>() {
                    @Override
                    public Iterator<AlertData> call(Iterator<String> iter) throws Exception {
                        Map<String, RuleInfo> rulesMap = (bcRules == null ? new HashMap<String, RuleInfo>() : bcRules.value());
                        Map<String, Set<String>> historyMap = (bcHistory == null ? new HashMap<String, Set<String>>() : bcHistory.value());

                        List<AlertData> out = new ArrayList<AlertData>();
                        while (iter.hasNext()) {
                            String line = iter.next();
                            if (line == null) continue;
                            JSONObject obj = null;
                            try {
                                obj = JSON.parseObject(line);
                            } catch (Exception e) {
                                // skip malformed
                                continue;
                            }
                            String sys = obj.getString("syscode");
                            String grp = obj.getString("groupcode");
                            String scase = obj.getString("systemcase");
                            String firstDim = obj.getString("firstDim");
                            String secDim = obj.getString("secDim");

                            if (sys == null || grp == null || scase == null || firstDim == null) continue;

                            // rule filter
                            String ruleKey = sys + "|" + grp + "|" + scase;
                            RuleInfo rule = rulesMap.get(ruleKey);
                            if (rule != null && rule.getAlertSpec() != null && rule.getAlertSpec().intValue() == -1) {
                                // no alert per rule
                                continue;
                            }

                            // history filter (if history says firstDim existed in last 14 days, skip)
                            Set<String> histSet = historyMap.get(ruleKey);
                            if (histSet != null && histSet.contains(firstDim)) {
                                // not new
                                continue;
                            }

                            // pass filters -> create AlertData
                            AlertData ad = new AlertData();
                            ad.setSyscode(sys);
                            ad.setGroupcode(grp);
                            ad.setSystemcase(scase);
                            ad.setFirstDim(firstDim);
                            if (secDim != null && secDim.length() > 0) ad.addSecDim(secDim);
                            out.add(ad);
                        }
                        return out.iterator();
                    }
                });
            }
        });

        // -------- window / aggregation --------
        // window length = 5 min, slide = 5 min (non-overlapping windows)
        JavaDStream<AlertData> windowed = filtered.window(Durations.minutes(5), Durations.minutes(5));

        // map to pair and reduceByKey to merge secDims per key
        JavaPairDStream<String, AlertData> pair = windowed.mapToPair(new PairFunction<AlertData, String, AlertData>() {
            @Override
            public Tuple2<String, AlertData> call(AlertData ad) throws Exception {
                String key = ad.getSyscode() + "|" + ad.getGroupcode() + "|" + ad.getSystemcase() + "|" + ad.getFirstDim();
                return new Tuple2<String, AlertData>(key, ad);
            }
        });

        JavaPairDStream<String, AlertData> reduced = pair.reduceByKey(new Function2<AlertData, AlertData, AlertData>() {
            @Override
            public AlertData call(AlertData a, AlertData b) throws Exception {
                a.merge(b);
                return a;
            }
        });

        // -------- final: for each RDD (i.e. each window execution), do dedup by (key + windowStart) and send alert --------
        reduced.foreachRDD(new VoidFunction2<JavaPairRDD<String, AlertData>, Time>() {
            @Override
            public void call(JavaPairRDD<String, AlertData> rdd, Time time) throws Exception {
                if (rdd == null) return;
                    rdd.foreach(new VoidFunction<Tuple2<String, AlertData>>() {
                        @Override
                        public void call(Tuple2<String, AlertData> rdd) throws Exception {
                            try {
//                                AlertSender.sendAlert(rdd._2);
                            } catch (Throwable sendEx) {
                                // log and continue
                                sendEx.printStackTrace();
                            }


                    };
                                });

                    // send alert (synchronous call here; consider async/queue in prod)


        }});


        jssc.start();
        jssc.awaitTermination();
    }
}
