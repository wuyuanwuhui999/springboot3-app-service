package com.player.prompt;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.http.client.HttpClientAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.player.prompt",exclude = { HttpClientAutoConfiguration.class, RestClientAutoConfiguration.class})
@MapperScan("com.player.prompt.mapper")
@EnableDiscoveryClient
public class PromptApplication {

    public static void main(String[] args) {
        SpringApplication.run(PromptApplication.class, args);
    }

}
