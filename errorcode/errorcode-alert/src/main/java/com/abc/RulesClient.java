package com.abc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * RulesClient - fetch full rules from RULES_API (no params)
 * Expected response: JSON Array like:
 * [
 *   {"syscode":"SDC2012120125","groupcode":"0003","systemcase":"...","alertSpec":1},
 *   ...
 * ]
 */
public class RulesClient {

    public static Map<String, RuleInfo> fetchAllRules(String rulesApiUrl) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(rulesApiUrl);
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
                JSONArray arr = JSONArray.parseArray(body);
                Map<String, RuleInfo> map = new HashMap<String, RuleInfo>();
                if (arr != null) {
                    for (int i = 0; i < arr.size(); i++) {
                        JSONObject o = arr.getJSONObject(i);
                        String sys = o.getString("syscode");
                        String grp = o.getString("groupcode");
                        String scase = o.getString("systemcase");
                        Integer alertSpec = null;
                        if (o.containsKey("alertSpec")) alertSpec = o.getInteger("alertSpec");
                        RuleInfo ri = new RuleInfo();
                        ri.setAlertSpec(alertSpec);
                        if (sys == null) sys = "";
                        if (grp == null) grp = "";
                        if (scase == null) scase = "";
                        String key = sys + "|" + grp + "|" + scase;
                        map.put(key, ri);
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
