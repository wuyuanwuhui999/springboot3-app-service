package com.player.gateway.common.utils;

import com.alibaba.fastjson2.JSON;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.web.util.UriComponentsBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class Common {
    /**
     * 获取当前时间字符串（线程安全）
     */
    public static String getFullTime(String pattern) {
        DateTimeFormatter formatter = (pattern != null)
                ? DateTimeFormatter.ofPattern(pattern)
                : DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }

    /**
     * 构建 POST 请求实体（优化 Content-Type 编码）
     */
    public static RequestEntity<String> postRequestEntity(String path, String token, Object params) {
        URI uri = UriComponentsBuilder.fromUriString(path).build().toUri();
        String jsonBody = JSON.toJSONString(params);
        return RequestEntity.post(uri)
                .accept(MediaType.APPLICATION_JSON)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", token)
                .body(jsonBody);
    }

    /**
     * 构建 PUT 请求实体
     */
    public static RequestEntity<String> putRequestEntity(String path, String token, Object params) {
        URI uri = UriComponentsBuilder.fromUriString(path).build().toUri();
        String jsonBody = JSON.toJSONString(params);
        return RequestEntity.put(uri)
                .accept(MediaType.APPLICATION_JSON)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", token)
                .body(jsonBody);
    }

    /**
     * 构建 DELETE 请求实体
     */
    public static RequestEntity<Void> deleteRequestEntity(String path, String token) {
        URI uri = UriComponentsBuilder.fromUriString(path).build().toUri();
        return RequestEntity.delete(uri)
                .accept(MediaType.APPLICATION_JSON)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", token)
                .build();
    }

    /**
     * 安全处理空字符串
     */
    public static String nullToString(String str) {
        return str == null ? "" : str;
    }

    /**
     * 生成图片文件（修复 JDK 17 的 Base64 解码问题）
     */
    public static String generateImage(String base64str, String savepath) {
        if (base64str == null) return null;

        try {
            // 使用 JDK 标准 Base64 解码
            byte[] imageBytes = Base64.getDecoder().decode(base64str.getBytes(StandardCharsets.UTF_8));

            // 读取图片尺寸
            try (InputStream is = new ByteArrayInputStream(imageBytes)) {
                BufferedImage image = ImageIO.read(is);
                if (image == null) {
                    throw new IOException("Invalid image data");
                }
                int width = image.getWidth();
                int height = image.getHeight();

                // 修改保存路径（添加尺寸）
                int dotIndex = savepath.lastIndexOf(".");
                String newPath = savepath.substring(0, dotIndex)
                        + "_" + width + "x" + height
                        + savepath.substring(dotIndex);

                // 写入文件（使用 try-with-resources 确保流关闭）
                try (OutputStream out = new FileOutputStream(newPath)) {
                    out.write(imageBytes);
                }
                return newPath.replaceAll(".+:", "");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}