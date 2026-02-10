package com.abc.errorcode.source;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HttpUtils {

    private static final CloseableHttpClient CLIENT =
            HttpClients.custom()
                    .setMaxConnTotal(100)
                    .setMaxConnPerRoute(20)
                    .build();

    public static String get(String url) throws IOException {
        HttpGet get = new HttpGet(url);
        get.setHeader("Accept", "application/json");

        try (CloseableHttpResponse resp = CLIENT.execute(get)) {
            HttpEntity entity = resp.getEntity();
            return EntityUtils.toString(entity, StandardCharsets.UTF_8);
        }
    }
}
