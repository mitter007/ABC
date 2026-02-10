package com.abc.es;

import com.alibaba.fastjson.JSON;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.elasticsearch.spark.rdd.api.java.JavaEsSpark;
import scala.Tuple2;

import java.util.*;

public class KafkaToES02 {

    /**
     * 参数说明（也可以通过 args 注入，这里简单示例默认值）：
     * - kafka.brokers
     * - kafka.topic (逗号分隔多个)
     * - group.id
     * - checkpoint.dir (必须在生产开启 checkpoint)
     * - es.nodes (逗号分隔)
     * - es.port
     * - es.user (可选)
     * - es.pass (可选)
     * - es.index (目标索引名)
     */
    public static void main(String[] args) throws Exception {

        // ---------- 配置（生产请通过 args 或配置中心传入） ----------
        final String kafkaBrokers = "kafka1:9092,kafka2:9092";
        final String kafkaTopics = "your_topic";
        final String kafkaGroupId = "spark-es-group";
        final String checkpointDir = "hdfs:///user/spark/checkpoint/kafka-to-es";

        final String esNodes = "es-node1,es-node2";
        final String esPort = "9200";
        final String esUser = "elastic";       // 如果不需要认证，设为 null 或 ""
        final String esPass = "your_password";
        final String esIndex = "window_index";

        // ---------- SparkConf ----------
        SparkConf conf = new SparkConf()
                .setAppName("KafkaToESApp")
                // 本地测试可保留 local[*], 集群提交删除下面一行
                .setMaster("local[*]");

        // ES 配置写入到 SparkConf（elasticsearch-spark 会读取）
        conf.set("es.nodes", esNodes);
        conf.set("es.port", esPort);
        conf.set("es.index.auto.create", "true");
        // 如果需要跨网络（NAT / WAN），开启
        conf.set("es.nodes.wan.only", "true");

        // 如果需要认证，设置用户名密码
        if (esUser != null && !esUser.isEmpty()) {
            conf.set("es.net.http.auth.user", esUser);
            conf.set("es.net.http.auth.pass", esPass);
        }

        // 建议设置 bulk 参数（按需调整）
        conf.set("es.batch.size.entries", "500");
        conf.set("es.batch.write.retry.count", "3");
        // 如果你在 map 中放 esId 字段来作为 _id，开启映射
        conf.set("es.mapping.id", "esId");

        JavaStreamingContext ssc = new JavaStreamingContext(conf, Durations.seconds(5));
        // 必须开启 checkpoint（推荐使用 HDFS）


        // ---------- Kafka 参数 ----------
        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers", kafkaBrokers);
        kafkaParams.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaParams.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaParams.put("group.id", kafkaGroupId);
        kafkaParams.put("auto.offset.reset", "latest");
        kafkaParams.put("enable.auto.commit", false);

        Collection<String> topics = Arrays.asList(kafkaTopics.split("\\s*,\\s*"));

        // ---------- 创建 Kafka DStream（注意泛型用 Object 以兼容 Spark 2.3.1） ----------
        JavaInputDStream<ConsumerRecord<Object, Object>> stream =
                KafkaUtils.createDirectStream(
                        ssc,
                        LocationStrategies.PreferConsistent(),
                        ConsumerStrategies.Subscribe(topics, kafkaParams)
                );

        // ---------- 把 ConsumerRecord<Object,Object> -> String(JSON) -> Map ----------
        JavaDStream<Map<String, Object>> esData = stream.map(record -> {
            String json = record.value().toString(); // 强制转 String
            Map<String, Object> map = JSON.parseObject(json, Map.class);

            // 补充字段：当前事件时间（毫秒）
            map.putIfAbsent("ts", System.currentTimeMillis());

            // 如果你想使用组合主键作为 ES _id (覆盖写)，请生成 esId 字段
            // 例如： id + "_" + name + "_" + windowStart
            Object idObj = map.get("id");
            Object nameObj = map.get("name");
            Object windowStartObj = map.get("windowStart");
            if (idObj != null && windowStartObj != null) {
                String esId = idObj.toString() + "_" + (nameObj == null ? "" : nameObj.toString()) + "_" + windowStartObj.toString();
                map.put("esId", esId);
            } else {
                // 保证每条都有 esId（可选：如果你不想覆盖写，可删除下面一行）
                map.putIfAbsent("esId", UUID.randomUUID().toString());
            }

            // 保证 windowStart/windowEnd 为 long（13 位毫秒）
            if (map.containsKey("windowStart")) {
                try {
                    map.put("windowStart", Long.parseLong(map.get("windowStart").toString()));
                } catch (Exception ignore) { }
            }
            if (map.containsKey("windowEnd")) {
                try {
                    map.put("windowEnd", Long.parseLong(map.get("windowEnd").toString()));
                } catch (Exception ignore) { }
            }

            return map;
        });

        // ---------- foreachRDD 写入 ES ----------
        esData.foreachRDD(rdd -> {
            rdd.foreachPartition(partition -> {

                List<Map<String, Object>> docs = new ArrayList<>();
                List<Map<String, String>> metas = new ArrayList<>();

                partition.forEachRemaining(record -> {
                    String date = record.get("kpidate") == null ? "unknown" : record.get("kpidate").toString();

                    // 动态索引： index_yyyy.MM.dd
                    String dynamicIndex = esIndex + "_" + date;

                    // meta 用于指定 index
                    Map<String, String> meta = new HashMap<>();
                    meta.put("index", dynamicIndex);
                    metas.add(meta);

                    docs.add(record);
                });

                // 批量写入 ES（不会创建新 RDD！！！）
                if (!docs.isEmpty()) {
                    JavaEsSpark.saveToEsWithMeta(
                            JavaSparkContext.fromSparkContext(rdd.context()).parallelizePairs(
                                    makePairs(metas, docs), 1
                            ),new HashMap<>()
                    );
                }
            });
        });


        // ---------- 启动 ----------
        ssc.start();
        ssc.awaitTermination();
    }
    // 辅助方法，把 meta 和 doc 组成 pair
    private static List<Tuple2<Object, Object>> makePairs(
            List<Map<String, String>> metas,
            List<Map<String, Object>> docs) {

        List<Tuple2<Object, Object>> list = new ArrayList<>();
        for (int i = 0; i < metas.size(); i++) {
            list.add(new Tuple2<>(metas.get(i), docs.get(i)));
        }
        return list;
    }

}
