package com.abc;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

/**
 * ClassName: HttpsUtils
 * Package: com.abc
 * Description:
 *
 * @Author JWT
 * @Create 2026/2/4 10:21
 * @Version 1.0
 */
public class HttpsUtils {
    public static void main(String[] args) throws IOException {
        HttpHost target = new HttpHost("1.2.3.4", 443, "https");

        HttpGet get = new HttpGet("/path");
        get.setHeader("Host", "abc.cn");

        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response = client.execute(target, get);

    }
}
