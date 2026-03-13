//package com.abc;
//
//import co.elastic.clients.elasticsearch.ElasticsearchClient;
//import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
//import co.elastic.clients.elasticsearch.core.SearchRequest;
//import co.elastic.clients.elasticsearch.core.SearchResponse;
//import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
//import java.util.ArrayList;
//
//public class YourService {
//    private final ElasticsearchClient client = ElasticsearchClientUtil.getClient();
//
//    public ArrayList<String> getDimcodeValus(String syscode, String groupCode, String otherDimcode, String dimcode, String code) {
//        // 1. 构建查询
//        BoolQuery boolQuery = new YourQueryBuilder().buildBoolQuery(syscode, groupCode, otherDimcode, dimcode, code);
//
//        // 2. 构建搜索请求
//        SearchRequest searchRequest = new SearchRequest.Builder()
//                .index(elasticSearchUtil.getIndex()) // 替换为你的索引获取逻辑
//                .query(boolQuery._toQuery())
//                .build();
//
//        // 3. 执行搜索
//        SearchResponse<TypeMapping> response;
//        try {
//            response = client.search(searchRequest, TypeMapping.class);
//        } catch (Exception e) {
//            // 处理异常
//            e.printStackTrace();
//            return new ArrayList<>();
//        }
//
//        // 4. 解析结果（示例：提取某个字段的值，需根据实际文档结构调整）
//        ArrayList<String> result = new ArrayList<>();
//        response.hits().hits().forEach(hit -> {
//            // 假设从_source中提取某个字段，如 "dimension.value"
//            // 若文档是Map结构，可使用 hit.source().get("dimension", Map.class).get("value")
//            // 这里以TypeMapping为例，实际需替换为你的文档类
//            result.add(hit.source().toString());
//        });
//
//        return result;
//    }
//}