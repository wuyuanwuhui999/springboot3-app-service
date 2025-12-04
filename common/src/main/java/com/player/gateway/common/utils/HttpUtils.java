package com.player.gateway.common.utils;
// Apache HttpClient 5.x
import com.player.gateway.common.entity.ResultEntity;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.Timeout;

// Spring Framework
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

// Servlet
import jakarta.servlet.http.HttpServletRequest;

// Fastjson
import com.alibaba.fastjson2.JSON;

// Java Standard Library
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

public class HttpUtils {

    /**
     * 创建线程安全的 HTTP 连接池
     */
    public static PoolingHttpClientConnectionManager getConnectionManager() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(100);            // 最大连接数
        cm.setDefaultMaxPerRoute(10);   // 每个路由最大连接数
        return cm;
    }

    /**
     * 通用 GET 请求（支持自定义 Host）
     */
    public static String doGet(String url, String host) {
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(getConnectionManager())
                .build()) {

            HttpGet httpGet = new HttpGet(url);
            configureHeaders(httpGet, host);
            httpGet.setConfig(getRequestConfig());

            return executeRequest(httpClient, httpGet);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String doGet(String url) {
        return doGet(url, "c.y.qq.com"); // 默认 Host
    }

    /**
     * 下载文件到本地
     */
    public static String doGetFile(String url, String savePath) {
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(getConnectionManager())
                .build()) {

            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) ...");
            httpGet.setConfig(getRequestConfig());

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                if (response.getCode() == HttpStatus.OK.value()) {
                    String ext = url.substring(url.lastIndexOf("."));
                    String fileName = UUID.randomUUID() + ext;
                    File outputFile = new File(savePath, fileName);

                    try (OutputStream out = new FileOutputStream(outputFile)) {
                        response.getEntity().writeTo(out);
                    }
                    return fileName;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 构建请求配置（超时设置）
     */
    public static RequestConfig getRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(Timeout.of(Duration.ofSeconds(1)))
                .setConnectionRequestTimeout(Timeout.of(Duration.ofMillis(500)))
                .setResponseTimeout(Timeout.of(Duration.ofSeconds(10)))
                .build();
    }

    /**
     * 获取请求完整路径
     */
    public static String getFullRequestPath(HttpServletRequest request) {
        String query = request.getQueryString();
        return request.getRequestURI() + (StringUtils.hasText(query) ? "?" + query : "");
    }

    /**
     * 生成随机字符串
     */
    public static String getRandomString() {
        return Long.toHexString(Double.doubleToLongBits(Math.random()));
    }

    /**
     * 通用 REST 请求工具
     */
    public static ResultEntity executeRestRequest(RestTemplate restTemplate, String url, String token,
                                                  HttpMethod method, Map<String, Object> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token); // 更规范的 Authorization 头设置

        HttpEntity<String> entity = new HttpEntity<>(JSON.toJSONString(params), headers);
        return restTemplate.exchange(url, method, entity, ResultEntity.class).getBody();
    }

    //-- 私有工具方法 --//
    private static void configureHeaders(HttpGet httpGet, String host) {
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) ...");
        httpGet.addHeader("Referer", "https://%s/".formatted(host));
        httpGet.addHeader("Host", host);
    }

    private static String executeRequest(CloseableHttpClient client, HttpGet request) throws IOException, ParseException {
        try (CloseableHttpResponse response = client.execute(request)) {
            if (response.getCode() == HttpStatus.OK.value()) {
                return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            }
        }
        return "";
    }
}