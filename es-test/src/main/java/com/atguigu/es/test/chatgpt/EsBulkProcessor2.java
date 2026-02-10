package com.atguigu.es.test.chatgpt;

import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EsBulkProcessor2 {

    private final RestHighLevelClient client;
    private BulkProcessor bulkProcessor;

    public EsBulkProcessor2(RestHighLevelClient client) {
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

    public  void bulkDeleteByUserNoRequest(String index, List<String> userNos) throws IOException {
        //创建ES客户端

            BulkProcessor.Listener listener = new BulkProcessor.Listener() {
                @Override
                public void beforeBulk(long executionId, BulkRequest request) {
                    // 执行之前调用
                    System.out.println("操作" + request.numberOfActions() + "条数据");
                }

                @Override
                public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                    // 执行之后调用
                    System.out.println("成功" + request.numberOfActions() + "条数据，用时" + response.getTook());
                }

                @Override
                public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                    // 失败时调用
                    System.out.println("失败" + request.numberOfActions() + "条数据");
                    System.out.println("失败" + failure);
                }
            };
            BulkProcessor bulkProcessor = BulkProcessor.builder(
                            (request, bulkListener) -> client.bulkAsync(request, RequestOptions.DEFAULT, bulkListener),
                            listener)
                    .setBulkActions(5000)
                    .setBulkSize(new ByteSizeValue(5L, ByteSizeUnit.MB))
                    .setFlushInterval(TimeValue.timeValueSeconds(10L))
                    .setConcurrentRequests(10)
                    .setBackoffPolicy(BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3))
                    .build();

            SearchRequest searchRequest = new SearchRequest(index);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.termsQuery("userNo", userNos));
            searchSourceBuilder.size(10000);
            searchRequest.source(searchSourceBuilder);

            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            if (searchResponse.getHits().getTotalHits().value == 0) {
                return;
            }
            if (searchResponse.getHits().getTotalHits().value > 0) {
                for (SearchHit hit : searchResponse.getHits().getHits()) {
                    bulkProcessor.add(new DeleteRequest(index).id(hit.getId()));
                }
                bulkProcessor.flush();
                bulkProcessor.close();
                client.close();
            }
        }


    public BulkProcessor getBulkProcessor() {
        return this.bulkProcessor;
    }

    public void close() throws InterruptedException {
        this.bulkProcessor.awaitClose(30, TimeUnit.SECONDS);
    }
}
