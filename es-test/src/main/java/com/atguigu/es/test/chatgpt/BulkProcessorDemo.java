package com.atguigu.es.test.chatgpt;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.RestClient;
import org.apache.http.HttpHost;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BulkProcessorDemo {
    public static void main(String[] args) throws Exception {
        // 1. 创建客户端
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("127.0.0.1", 9200, "http"))
        );

        String indexName = "shopping";

        // 2. 检查索引
        EsIndexUtil indexUtil = new EsIndexUtil(client);
        indexUtil.createIndexIfNotExists(indexName);

        // 3. 创建 BulkProcessor
        EsBulkProcessor bulkUtil = new EsBulkProcessor(client);
        BulkProcessor bulkProcessor = bulkUtil.getBulkProcessor();

        // 4. 添加数据
        for (int i = 1; i <= 5000; i++) {
            Map<String, Object> doc = new HashMap<>();
            doc.put("name", "item_" + i);
            doc.put("price", i * 1.5);
            doc.put("stock", i * 10);

            bulkProcessor.add(new IndexRequest(indexName).id(String.valueOf(i)).source(doc));
        }

        // 5. 关闭
        bulkUtil.close();
        client.close();
    }
}
