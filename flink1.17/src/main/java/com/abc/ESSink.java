package com.abc;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ESSink extends RichSinkFunction<Map<String, Object>> {

    private static final Logger log = LoggerFactory.getLogger(ESSink.class);

    private transient RestHighLevelClient client;
    private transient BulkProcessor bulkProcessor;

    private final String index;

    public ESSink(String index) {
        this.index = index;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);

        // ----------------------
        // 创建 Elasticsearch Client
        // ----------------------
        client = new RestHighLevelClient(
                RestClient.builder(
                        // TODO：改成你的 ES 集群地址
                        new org.apache.http.HttpHost("your-es-host", 9200, "http")
                )
        );

        // ----------------------
        // 创建 BulkProcessor
        // ----------------------
        BulkProcessor.Listener listener = new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
                log.info("Bulk [{}] ready to execute {} actions", executionId, request.numberOfActions());
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                if (response.hasFailures()) {
                    log.error("Bulk [{}] executed with failures: {}", executionId, response.buildFailureMessage());
                } else {
                    log.info("Bulk [{}] executed successfully in {} ms",
                            executionId, response.getTook().getMillis());
                }
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                log.error("Bulk [{}] failed", executionId, failure);
            }
        };

        BulkProcessor.Builder builder = BulkProcessor.builder(
                (request, bulkListener) ->
                        client.bulkAsync(request, RequestOptions.DEFAULT, bulkListener),
                listener);

        // 批量大小：5MB
        builder.setBulkSize(new ByteSizeValue(5, ByteSizeUnit.MB));

        // 批量条数：500 条
        builder.setBulkActions(500);

        // 最大等待 2 秒
        builder.setFlushInterval(TimeValue.timeValueSeconds(2));

        // 并发 bulk
        builder.setConcurrentRequests(2);

        // 重试策略
        builder.setBackoffPolicy(BackoffPolicy.exponentialBackoff(
                TimeValue.timeValueMillis(200), 3
        ));

        bulkProcessor = builder.build();

        log.info("BulkProcessor for index [{}] initialized.", index);
    }

    @Override
    public void invoke(Map<String, Object> value, Context context) throws Exception {

        IndexRequest request = new IndexRequest(index)
                .source(value);

        bulkProcessor.add(request);
    }

    @Override
    public void close() throws Exception {
        super.close();
        if (bulkProcessor != null) {
            bulkProcessor.flush();
            bulkProcessor.close();
        }
        if (client != null) {
            client.close();
        }
    }
}
