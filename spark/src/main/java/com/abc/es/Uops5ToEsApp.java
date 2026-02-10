package com.abc.es;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
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

import java.text.SimpleDateFormat;
import java.util.*;

public class Uops5ToEsApp {

    public static void main(String[] args) throws Exception {

        // ---------------- Spark 配置 ----------------
        SparkConf conf = new SparkConf()
                .setAppName("Uops5ToEsApp")
                .setMaster("local[*]")
                // ES相关配置
                .set("es.nodes", "127.0.0.1:9200")
                .set("es.index.auto.create", "true");

        JavaStreamingContext ssc = new JavaStreamingContext(conf, Durations.seconds(5));

        // ---------------- Kafka 配置 ----------------
        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers", "127.0.0.1:9092");
        kafkaParams.put("key.deserializer", StringDeserializer.class);
        kafkaParams.put("value.deserializer", StringDeserializer.class);
        kafkaParams.put("group.id", "uops5_group");
        kafkaParams.put("auto.offset.reset", "latest");
        kafkaParams.put("enable.auto.commit", false);

        Collection<String> topics = Arrays.asList("uops5");

        JavaInputDStream<ConsumerRecord<String, String>> stream = KafkaUtils.createDirectStream(
                ssc,
                LocationStrategies.PreferConsistent(),
                ConsumerStrategies.Subscribe(topics, kafkaParams)
        );

        // ---------------- 处理 kafka 数据 ----------------
        JavaDStream<String> map = stream.map(ConsumerRecord::value);
        stream.map(ConsumerRecord::value)
                .foreachRDD(rdd -> {



                    // 每条数据都是 JSON 字符串
                    rdd.foreachPartition(partition -> {

                        List<Tuple2<Object, Object>> buffer = new ArrayList<>();

                        while (partition.hasNext()) {
                            String jsonStr = partition.next();

                            JSONObject obj;
                            try {
                                obj = JSON.parseObject(jsonStr);
                            } catch (Exception e) {
                                continue; // 解析失败跳过
                            }

                            String kpidate = obj.getString("kpidate");
                            if (kpidate == null || kpidate.length() != 8) {
                                kpidate = "unknown";
                            }

                            // yyyyMMdd → yyyy.MM.dd
                            String indexDate = formatDate(kpidate);

                            String dynamicIndex = "uops5_index_" + indexDate;

                            Map<String, String> meta = new HashMap<>();
                            meta.put("index", dynamicIndex);

                            // 注意：写入 ES 是 map
                            Map<String, Object> doc = new HashMap<>(obj);

                            buffer.add(new Tuple2<>(meta, doc));
                        }

                        if (!buffer.isEmpty()) {

                            JavaSparkContext jsc =
                                    JavaSparkContext.fromSparkContext(rdd.context());

                            JavaPairRDD<Object, Object> esRDD =
                                    jsc.parallelizePairs(buffer, 1);

                            // ★ 必须两个参数
                            JavaEsSpark.saveToEsWithMeta(esRDD, new HashMap<>());
                        }

                    });


                });

        ssc.start();
        ssc.awaitTermination();
    }

    // yyyyMMdd → yyyy.MM.dd
    private static String formatDate(String yyyyMMdd) {
        try {
            SimpleDateFormat src = new SimpleDateFormat("yyyyMMdd");
            SimpleDateFormat dst = new SimpleDateFormat("yyyy.MM.dd");
            return dst.format(src.parse(yyyyMMdd));
        } catch (Exception e) {
            return "unknown";
        }
    }
}
