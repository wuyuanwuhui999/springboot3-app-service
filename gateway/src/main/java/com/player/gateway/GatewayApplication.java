package com.player.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.http.client.HttpClientAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;

@SpringBootApplication(scanBasePackages = "com.player.gateway",
        exclude = {
                org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration.class // 不需要排除，我们要用 MongoDB
        })
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}
