package com.atguigu.es.test.chat2;

import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EsTestInsert {
    public static void main(String[] args) {
        // 1. 创建客户端
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http"))
        );

        try {
            // 2. 构造要写入的数据
            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("groupcode", "G001");
            jsonMap.put("syscode", "SYS01");
            jsonMap.put("dimcode", "D001");
            jsonMap.put("dimcont", "测试维度内容");
            jsonMap.put("sys_succrate_60", 97.5);
            jsonMap.put("tps_60", 123);
            jsonMap.put("@timestamp", "2025-09-27 08:30:00"); // 格式要和 mapping 中定义的一致

            // 3. 构造 IndexRequest
            IndexRequest request = new IndexRequest("kafka-asmp-log_0927")
                    .source(jsonMap);

            // 4. 执行写入
            IndexResponse response = client.index(request, RequestOptions.DEFAULT);

            System.out.println("写入结果: " + response.getResult());
            System.out.println("文档ID: " + response.getId());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 5. 关闭客户端
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
