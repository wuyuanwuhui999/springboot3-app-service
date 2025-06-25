package com.player.music;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.http.client.HttpClientAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;

@SpringBootApplication(
        scanBasePackages = "com.player.music",exclude = {
            HttpClientAutoConfiguration.class,
            RestClientAutoConfiguration.class,
            org.springframework.ai.autoconfigure.vectorstore.chroma.ChromaVectorStoreAutoConfiguration.class
        }
)
@MapperScan("com.player.music.mapper")
public class MusicApplication {

    public static void main(String[] args) {
        SpringApplication.run(MusicApplication.class, args);
    }

}
