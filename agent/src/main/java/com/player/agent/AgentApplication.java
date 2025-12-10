package com.player.agent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.http.client.HttpClientAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;

@SpringBootApplication(
        scanBasePackages = "com.player.agent",exclude = {
        HttpClientAutoConfiguration.class,
        RestClientAutoConfiguration.class,
        OpenAiAutoConfiguration.class,
        org.springframework.ai.autoconfigure.vectorstore.elasticsearch.ElasticsearchVectorStoreAutoConfiguration.class
}
)
@MapperScan("com.player.agent.mapper")
public class AgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentApplication.class, args);
    }

}
