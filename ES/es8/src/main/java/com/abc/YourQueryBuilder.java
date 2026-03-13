package com.abc;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import org.apache.commons.lang3.StringUtils;

public class YourQueryBuilder {

    /**
     * 构建布尔查询（新版API）
     */
    public BoolQuery buildBoolQuery(String sysCode, String groupCode, String otherDimcode, String dimcode, String code) {
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        // 1. 过滤 syscode.keyword
        TermQuery sysCodeTerm = TermQuery.of(t -> t.field("syscode.keyword").value(sysCode));
        boolQueryBuilder.filter(sysCodeTerm._toQuery());

        // 2. 过滤 groupcode.keyword
        TermQuery groupCodeTerm = TermQuery.of(t -> t.field("groupcode.keyword").value(groupCode));
        boolQueryBuilder.filter(groupCodeTerm._toQuery());

        // 3. 过滤 dimension.{dimcode}.keyword
        String dimField = "dimension." + dimcode + ".keyword";
        TermQuery dimCodeTerm = TermQuery.of(t -> t.field(dimField).value(code));
        boolQueryBuilder.filter(dimCodeTerm._toQuery());

        // 4. 处理 otherDimcode（非空时添加过滤）
        if (!StringUtils.isEmpty(otherDimcode)) {
            String otherDimField = "dimension." + otherDimcode + ".keyword";
            TermQuery otherDimTerm = TermQuery.of(t -> t.field(otherDimField).value("")); // 注意：原代码中value被截断，需根据实际业务补全
            boolQueryBuilder.filter(otherDimTerm._toQuery());
        }

        return boolQueryBuilder.build();
    }

    // ... 其他方法
}