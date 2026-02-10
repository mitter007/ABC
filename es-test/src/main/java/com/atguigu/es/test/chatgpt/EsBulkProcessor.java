package com.atguigu.es.test.chatgpt;

import org.elasticsearch.action.bulk.*;
import org.elasticsearch.client.*;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;

import java.util.concurrent.TimeUnit;

public class EsBulkProcessor {

    private final RestHighLevelClient client;
    private BulkProcessor bulkProcessor;

    public EsBulkProcessor(RestHighLevelClient client) {
        this.client = client;
        initBulkProcessor();
    }

    private void initBulkProcessor() {
        BulkProcessor.Listener listener = new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
                System.out.println("【Bulk开始】request size=" + request.numberOfActions());
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                if (response.hasFailures()) {
                    System.err.println("【Bulk失败】" + response.buildFailureMessage());
                } else {
                    System.out.println("【Bulk成功】" + response.getItems().length + " 条");
                }
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                System.err.println("【Bulk异常】" + failure.getMessage());
            }
        };

        BulkProcessor.Builder builder = BulkProcessor.builder(
                (request, bulkListener) -> client.bulkAsync(request, RequestOptions.DEFAULT, bulkListener),
                listener
        );

        // 配置策略
        builder.setBulkActions(1000); // 每 1000 条触发一次
        builder.setBulkSize(new ByteSizeValue(5, ByteSizeUnit.MB)); // 每 5MB 触发一次
        builder.setFlushInterval(TimeValue.timeValueSeconds(10)); // 每 10s 强制 flush
        builder.setConcurrentRequests(2); // 并发请求数

        this.bulkProcessor = builder.build();
    }

    public BulkProcessor getBulkProcessor() {
        return this.bulkProcessor;
    }

    public void close() throws InterruptedException {
        this.bulkProcessor.awaitClose(30, TimeUnit.SECONDS);
    }
}
