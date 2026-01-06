package com.player.chat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.http.client.HttpClientAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Lazy;

@SpringBootApplication(scanBasePackages = {"com.player.chat", "org.springframework.ai.chat.client"},exclude = { HttpClientAutoConfiguration.class, RestClientAutoConfiguration.class})
@MapperScan("com.player.chat.mapper")
@Lazy
@EnableDiscoveryClient  // 启用服务发现
public class ChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class, args);
    }

}