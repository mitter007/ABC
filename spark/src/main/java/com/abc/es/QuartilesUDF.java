package com.abc.es;

import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.api.java.UDF2;
import scala.Double;
import scala.collection.JavaConverters;
import scala.collection.Map;
import scala.collection.mutable.WrappedArray;

import java.util.List;
import java.util.Objects;

public class QuartilesUDF implements UDF1<WrappedArray<Double>, List<Double>> {
    private static Integer day;

    public QuartilesUDF() {
    }

    @Override
    public List<Double> call(WrappedArray<Double> doubleWrappedArray) throws Exception {
        return null;
    }
}
