package com.mojian.utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;

/**
 * @className: HttpRequestUtils
 * @author: Icw
 * @date: 2025/4/20 1:55
 * @Version: 1.0
 * @description:
 */

public class HttpRequestUtils {
    public static HttpResponse doGet(String url) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        HttpResponse execute = client.execute(request);
        return execute;
    }
 }
