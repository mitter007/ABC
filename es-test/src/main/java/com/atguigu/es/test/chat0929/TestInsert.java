package com.atguigu.es.test.chat0929;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestInsert {
    public static void main(String[] args) throws Exception {

        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http"))
        );
        new TestInsert().parseSearchResponse();
        SearchResponse searchResponse = new TestInsert().queryDimension( client);
        SearchHit[] hits = searchResponse.getHits().getHits();
        for (SearchHit hit : hits) {

            System.out.println(hit.getSourceAsMap());
        }
        client.close();

    }

    public SearchResponse queryDimension(RestHighLevelClient client) throws Exception {
        // 构建 bool 查询
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery("dimension.proCod.keyword", "TOTAL"))
                .filter(QueryBuilders.termQuery("syscode.keyword", "SYS002"))
                .filter(QueryBuilders.termQuery("groupcode.keyword", "GRP002"))
                .mustNot(QueryBuilders.termQuery("dimension.channelCode.keyword", "TOTAL"));

        // 构建查询请求
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(boolQuery);

        SearchRequest searchRequest = new SearchRequest("kafka-asmp-0928"); // 索引名
        searchRequest.source(sourceBuilder);

        // 执行查询
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        return searchResponse;
    }

    public void parseSearchResponse() throws IOException {
        List<EsRecord> list = new ArrayList<>();

        EsRecord rec1 = new EsRecord();
        rec1.setTimestamp("2025-09-28 12:00:00");
        rec1.setSyscode("SYS001");
        rec1.setGroupcode("GRP001");
        rec1.setDimcode("DC001");
        rec1.setDimcont("维度1");
        rec1.setSysSuccrate60(98.5);
        rec1.setTps60(1234L);

        EsRecord.Dimension dim1 = new EsRecord.Dimension();
        dim1.setChannelCode("TOTAL");
        dim1.setProCod("PRD01");
        dim1.setTransCode("TRX01");
        rec1.setDimension(dim1);

        list.add(rec1);

        EsRecord rec2 = new EsRecord();
        rec2.setTimestamp("2025-09-28 12:05:00");
        rec2.setSyscode("SYS002");
        rec2.setGroupcode("GRP002");
        rec2.setDimcode("DC002");
        rec2.setDimcont("维度2");
        rec2.setSysSuccrate60(97.2);
        rec2.setTps60(567L);

        EsRecord.Dimension dim2 = new EsRecord.Dimension();
        dim2.setChannelCode("CH02");
        dim2.setProCod("TOTAL");
        dim2.setTransCode("TRX02");
        rec2.setDimension(dim2);

        list.add(rec2);
        EsRecord rec3 = new EsRecord();
        rec3.setTimestamp("2025-09-28 12:05:00");
        rec3.setSyscode("SYS002");
        rec3.setGroupcode("GRP002");
        rec3.setDimcode("DC002");
        rec3.setDimcont("维度2");
        rec3.setSysSuccrate60(97.2);
        rec3.setTps60(567L);

        EsRecord.Dimension dim3 = new EsRecord.Dimension();
        dim3.setChannelCode("TOTAL");
        dim3.setProCod("TOTAL");
        dim3.setTransCode("TRX02");
        rec3.setDimension(dim3);

        list.add(rec3);

        // 批量插入
        EsBulkInsertUtil.bulkInsert("kafka-asmp-0928", list);

    }

    public SearchResponse queryData(RestHighLevelClient client) throws Exception {
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
