package com.abc.udf;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.api.java.UDF1;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;
import scala.collection.Map;
import scala.collection.JavaConverters;

import java.util.HashMap;
/**
 * ClassName: MapUdf
 * Package: com.abc.udf
 * Description:
 *
 * @Author JWT
 * @Create 2026/1/27 20:20
 * @Version 1.0
 */
public class MapUdf implements UDF1<Map<String, Row>, String> {
    ObjectMapper mapper = new ObjectMapper();
    @Override
    public String call(Map<String, Row> map) throws Exception {
        if (map == null) {
            return null;
        }

        java.util.Map<String, Object> result = new HashMap<>();

        // Scala Map → Java Map
        java.util.Map<String, Row> javaMap =
                JavaConverters.mapAsJavaMapConverter(map).asJava();

        for (java.util.Map.Entry<String, Row> entry : javaMap.entrySet()) {
            Row v = entry.getValue();

            java.util.Map<String, Object> metrics = new HashMap<>();
            metrics.put("avg_tps_win", v.getAs("avg_tps_win"));
            metrics.put("stddev_tps_win", v.getAs("stddev_tps_win"));
            metrics.put("sample_cnt_win", v.getAs("sample_cnt_win"));

            result.put(entry.getKey(), metrics);
        }
        return mapper.writeValueAsString(result);
    }
}
