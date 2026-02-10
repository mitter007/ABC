package com.atguigu.es.test.chat3;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EsQueryService {

    private final RestHighLevelClient client;

    public EsQueryService(RestHighLevelClient client) {
        this.client = client;
    }

    public List<EsRecord> queryData(String indexName, String syscode, String groupcode, String dimcont) throws IOException {
        List<EsRecord> resultList = new ArrayList<>();

        // 构建查询条件
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("syscode.keyword", syscode))
                .must(QueryBuilders.termQuery("groupcode.keyword", groupcode))
                .must(QueryBuilders.termQuery("dimcont.keyword", dimcont));

        // 构建查询请求
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(boolQuery)
                .fetchSource(new String[]{"dctime", "tps_60", "avg_time", "sys_succrate_60"}, null)
                .size(1000); // 可调整返回数量

        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source(sourceBuilder);

        // 执行查询
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        // 封装结果
        for (SearchHit hit : response.getHits().getHits()) {
            Map<String, Object> source = hit.getSourceAsMap();

            EsRecord record = new EsRecord();
            record.setSyscode(syscode);
            record.setGroupcode(groupcode);
            record.setDimcont(dimcont);

            record.setDctime((String) source.get("dctime"));
            record.setTps60(source.get("tps_60") == null ? null : ((Number) source.get("tps_60")).doubleValue());
            record.setAvgTime(source.get("avg_time") == null ? null : ((Number) source.get("avg_time")).doubleValue());
            record.setSysSuccrate60(source.get("sys_succrate_60") == null ? null : ((Number) source.get("sys_succrate_60")).doubleValue());

            resultList.add(record);
        }

        return resultList;
    }
}
