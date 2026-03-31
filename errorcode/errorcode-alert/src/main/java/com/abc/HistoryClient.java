package com.abc;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * HistoryClient - fetch full 14-day history from HISTORY_API
 *
 * Expected response: either
 * 1) a JSON object: { "SDC|0003|systemcase": ["err1","err2"], ... }
 * or another structure - adjust parsing accordingly.
 */
public class HistoryClient {

    public static Map<String, Set<String>> fetchAllHistory(String historyApiUrl) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(historyApiUrl);
        try {
            CloseableHttpResponse resp = client.execute(get);
            try {
                int sc = resp.getStatusLine().getStatusCode();
                if (sc != 200) return null;
                BufferedReader br = new BufferedReader(new InputStreamReader(resp.getEntity().getContent(), "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                String body = sb.toString();
                JSONObject obj = JSONObject.parseObject(body);
                Map<String, Set<String>> map = new HashMap<String, Set<String>>();
                if (obj != null) {
                    for (String key : obj.keySet()) {
                        Set<String> set = new HashSet<String>();
                        try {
                            for (Object v : obj.getJSONArray(key)) set.add(String.valueOf(v));
                        } catch (Exception e) {
                            // ignore parse errors for this key
                        }
                        map.put(key, set);
                    }
                }
                return map;
            } finally {
                resp.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            try { client.close(); } catch (Exception e) {}
        }
    }
}
