package com.abc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * JsonUtil - tiny helpers
 */
public class JsonUtil {
    public static JSONObject parse(String json) {
        try {
            return JSON.parseObject(json);
        } catch (Exception e) {
            return null;
        }
    }
}
