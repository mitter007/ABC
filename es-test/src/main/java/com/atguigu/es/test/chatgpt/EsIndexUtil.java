package com.atguigu.es.test.chatgpt;

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;

import java.io.IOException;

public class EsIndexUtil {

    private final RestHighLevelClient client;

    public EsIndexUtil(RestHighLevelClient client) {
        this.client = client;
    }

    public void createIndexIfNotExists(String indexName) throws IOException {
        GetIndexRequest getRequest = new GetIndexRequest(indexName);
        boolean exists = client.indices().exists(getRequest, RequestOptions.DEFAULT);

        if (!exists) {
            CreateIndexRequest createRequest = new CreateIndexRequest(indexName);
            createRequest.settings(org.elasticsearch.common.settings.Settings.builder()
                    .put("index.number_of_shards", 1)
                    .put("index.number_of_replicas", 1)
            );
            CreateIndexResponse response = client.indices().create(createRequest, RequestOptions.DEFAULT);
            System.out.println("【索引创建】" + indexName + " ack=" + response.isAcknowledged());
        } else {
            System.out.println("【索引已存在】" + indexName);
        }
    }
}
