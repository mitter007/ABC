package com.abc.spark;

import org.apache.spark.sql.api.java.UDF1;

import java.util.Map;

/**
 * ClassName: QuartilesUDF
 * Package: com.abc.spark
 * Description:
 *
 * @Author JWT
 * @Create 2026/1/23 17:31
 * @Version 1.0
 */
public class QuartilesUDF implements UDF1<Map<String,Double>,String> {
    @Override
    public String call(Map<String, Double> stringDoubleMap) throws Exception {
        return null;
    }
}
