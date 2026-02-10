package com.atguigu.es.test.chat0929;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.util.List;

public class EsBulkInsertUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 批量插入方法
    public static void bulkInsert(String index, List<EsRecord> records) throws IOException {
        try (RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("127.0.0.1", 9200, "http")))) {

            BulkRequest bulkRequest = new BulkRequest();

            for (EsRecord record : records) {
                String json = objectMapper.writeValueAsString(record);
                bulkRequest.add(new IndexRequest(index).source(json, XContentType.JSON));
            }

            BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
            if (bulkResponse.hasFailures()) {
                System.err.println("部分插入失败: " + bulkResponse.buildFailureMessage());
            } else {
                System.out.println("批量插入成功, 共 " + records.size() + " 条");
            }
        }
    }
}
