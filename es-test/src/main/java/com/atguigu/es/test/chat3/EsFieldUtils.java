package com.atguigu.es.test.chat3;

public class EsFieldUtils {

    /**
     * 处理 dctime 字段，截取前 12 位
     * @param value ES 返回的 dctime，可能是 String 或 Number
     * @return 前 12 位字符串，如果不足 12 位就返回原始值；为空时返回 null
     */
    public static String formatDctime(Object value) {
        if (value == null) {
            return null;
        }

        String dctimeStr;
        if (value instanceof Number) {
            dctimeStr = String.valueOf(value);
        } else {
            dctimeStr = value.toString();
        }

        if (dctimeStr.length() >= 12) {
            return dctimeStr.substring(0, 12);
        } else {
            return dctimeStr;
        }
    }
}
