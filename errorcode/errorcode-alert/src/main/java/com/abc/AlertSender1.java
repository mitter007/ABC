package com.abc;

import com.alibaba.fastjson.JSON;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;

/**
 * AlertSender - POST alert payload to ALERT_API_URL
 */
public class AlertSender1 {

    public static class AlertPayload {
        private String syscode;
        private String groupcode;
        private String systemcase;
        private String firstDim;
        private List<String> secDims;
        private Date windowTime;

        public String getSyscode() {
            return syscode;
        }

        public void setSyscode(String syscode) {
            this.syscode = syscode;
        }

        public String getGroupcode() {
            return groupcode;
        }

        public void setGroupcode(String groupcode) {
            this.groupcode = groupcode;
        }

        public String getSystemcase() {
            return systemcase;
        }

        public void setSystemcase(String systemcase) {
            this.systemcase = systemcase;
        }

        public String getFirstDim() {
            return firstDim;
        }

        public void setFirstDim(String firstDim) {
            this.firstDim = firstDim;
        }

        public List<String> getSecDims() {
            return secDims;
        }

        public void setSecDims(List<String> secDims) {
            this.secDims = secDims;
        }

        public Date getWindowTime() {
            return windowTime;
        }

        public void setWindowTime(Date windowTime) {
            this.windowTime = windowTime;
        }


        public static void sendAlert(String url, AlertPayload payload) {
            CloseableHttpClient client = HttpClients.createDefault();
            try {
                HttpPost post = new HttpPost(url);
                post.addHeader("Content-Type", "application/json;charset=UTF-8");
                String body = JSON.toJSONString(payload);
                post.setEntity(new StringEntity(body, "UTF-8"));
                CloseableHttpResponse resp = client.execute(post);
                try {
                    int sc = resp.getStatusLine().getStatusCode();
                    BufferedReader br = new BufferedReader(new InputStreamReader(resp.getEntity().getContent(), "UTF-8"));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                    // optionally log response
                    // System.out.println("Alert send status: " + sc + " body:" + sb.toString());
                } finally {
                    resp.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    client.close();
                } catch (Exception e) {
                }
            }
        }

        public void sendAlert() {
        }
    }
}
