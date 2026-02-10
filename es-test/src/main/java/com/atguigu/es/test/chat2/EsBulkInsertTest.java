package com.atguigu.es.test.chat2;

import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EsBulkInsertTest {
    public static void main(String[] args) {
        // 1. 创建客户端
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http"))
        );

        try {
            // 2. 创建 BulkRequest
            BulkRequest bulkRequest = new BulkRequest();

            // 模拟插入 10 条数据
            for (int i = 1; i <= 10; i++) {
                Map<String, Object> jsonMap = new HashMap<>();
                jsonMap.put("groupcode", "G00" + i);
                jsonMap.put("syscode", "SYS" + i);
                jsonMap.put("dimcode", "D00" + i);
                jsonMap.put("dimcont", "测试维度内容 " + i);
//                jsonMap.put("sys_succrate_60", 90 + i * 0.5);
//                jsonMap.put("tps_60", 50 + i *2 );

                // 时间戳（当前时间）
                String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis() - 24L * 60 * 60 * 1000));
                jsonMap.put("@timestamp", now);

                IndexRequest request = new IndexRequest("kafka-asmp-log_0927")
                        .source(jsonMap);

                bulkRequest.add(request);
            }

            // 3. 执行批量写入
            BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);

            if (bulkResponse.hasFailures()) {
                System.out.println("部分写入失败: " + bulkResponse.buildFailureMessage());
            } else {
                System.out.println("批量写入成功，共写入 " + bulkResponse.getItems().length + " 条数据");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
