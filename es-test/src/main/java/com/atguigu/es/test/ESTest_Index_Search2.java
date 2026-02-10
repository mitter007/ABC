package com.atguigu.es.test;

import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentFactory;

public class ESTest_Index_Search2 {
    public static void main(String[] args) throws Exception {

        RestHighLevelClient esClient = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http"))
        );

        BulkProcessor.Listener listener = new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
                System.out.println("准备执行批量操作, 数量: " + request.numberOfActions());
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                if (response.hasFailures()) {
                    System.out.println("批量操作有失败: " + response.buildFailureMessage());
                } else {
                    System.out.println("批量操作成功!");
                }
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                System.err.println("批量操作异常: " + failure.getMessage());
            }
        };

        BulkProcessor bulkProcessor = BulkProcessor.builder(
                        (request, bulkListener) -> esClient.bulkAsync(request, RequestOptions.DEFAULT, bulkListener),
                        listener)
                .setBulkActions(1000)          // 满 1000 条请求就提交
                .setBulkSize(new ByteSizeValue(5, ByteSizeUnit.MB)) // 达到 5MB 就提交
                .setFlushInterval(TimeValue.timeValueSeconds(5))    // 每 5 秒提交一次
                .setConcurrentRequests(2)     // 允许 2 个并发请求
                .build();

// 添加数据
        for (int i = 0; i < 10000; i++) {
            IndexRequest request = new IndexRequest("test")
                    .id(String.valueOf(i))
                    .source(XContentFactory.jsonBuilder()
                            .startObject()
                            .field("field", i)
                            .endObject());
            bulkProcessor.add(request);
        }

// 程序结束时关闭
        bulkProcessor.close();
        esClient.close();

        esClient.close();
    }
}
