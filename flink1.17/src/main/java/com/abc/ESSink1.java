package com.abc;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

// 使用 relocated 的包名（注意：shade.es742...）
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.TimeValue;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ESSink1 extends RichSinkFunction<Map<String, Object>> {

    private transient RestHighLevelClient client;
    private transient BulkProcessor bulkProcessor;

    private final String[] hosts;
    private final String index;

    public ESSink1(String index, String... hosts) {
        this.index = index;
        this.hosts = hosts;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);

        // RestClient builder from relocated package names
        RestClientBuilder builder = RestClient.builder(
                // 假设只有一个 host，或构建多个 HttpHost
                new HttpHost(hosts[0].split(":")[0],
                        Integer.parseInt(hosts[0].split(":")[1]),
                        "http")
        );

        client = new RestHighLevelClient(builder);

        // BulkProcessor
        bulkProcessor = BulkProcessor.builder(
                (bulkRequest, bulkListener) -> client.bulkAsync(bulkRequest, RequestOptions.DEFAULT, bulkListener),
                new BulkProcessor.Listener() {
                    @Override public void beforeBulk(long executionId, BulkRequest request) {}
                    @Override public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                        if (response.hasFailures()) {
                            System.err.println("ES bulk failures: " + response.buildFailureMessage());
                        }
                    }
                    @Override public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                        failure.printStackTrace();
                    }
                })

                .setBulkActions(500)
                .setBulkSize(new ByteSizeValue(5, ByteSizeUnit.MB))
                .setConcurrentRequests(2)
                .setFlushInterval(TimeValue.timeValueSeconds(1))
                .setBackoffPolicy(BackoffPolicy.constantBackoff(TimeValue.timeValueSeconds(1), 3))
                .build();
    }

    @Override
    public void invoke(Map<String, Object> value, Context context) throws Exception {
        IndexRequest request = new IndexRequest(index).source(value);
        bulkProcessor.add(request);
    }

    @Override
    public void close() throws Exception {
        super.close();
        if (bulkProcessor != null) {
            bulkProcessor.flush();
            bulkProcessor.awaitClose(30, TimeUnit.SECONDS);
        }
        if (client != null) {
            client.close();
        }
    }
}
