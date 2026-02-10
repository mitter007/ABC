package com.atguigu.es.test.chat0929;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public class EsQueryExample {

    private final RestHighLevelClient client;

    public EsQueryExample(RestHighLevelClient client) {
        this.client = client;
    }

    public SearchResponse queryData() throws Exception {
        // 构建 bool 查询
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery("dimension.proCod.keyword", "TOTAL"))
                .filter(QueryBuilders.termQuery("syscode.keyword", "SYS002"))
                .filter(QueryBuilders.termQuery("groupcode.keyword", "GRP002"))
                .mustNot(QueryBuilders.termQuery("dimension.channelCode.keyword", "TOTAL"));

        // 构建 SearchSourceBuilder
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(boolQuery);

        // 指定索引
        SearchRequest searchRequest = new SearchRequest("kafka-asmp-0928");
        searchRequest.source(sourceBuilder);

        // 执行查询
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        return response;
    }
}
