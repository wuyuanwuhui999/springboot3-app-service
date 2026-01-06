package com.player.circle;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.http.client.HttpClientAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.player.circle",exclude = { HttpClientAutoConfiguration.class, RestClientAutoConfiguration.class})
@MapperScan("com.player.circle.mapper")
@EnableDiscoveryClient  // 只需要添加这个注解即可
public class CircleApplication {

    public static void main(String[] args) {
        SpringApplication.run(CircleApplication.class, args);
    }

}
