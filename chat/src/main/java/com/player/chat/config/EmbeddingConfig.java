package com.player.chat.config;

import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.http.client.spring.restclient.SpringRestClient;
import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilderFactory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class EmbeddingConfig {

    @Value("${nomic.embedding.model-name}")
    private String modelName;

    @Value("${langchain4j.ollama.chat-model.base-url}")
    private String baseUrl;

    @Value("${nomic.embedding.timeout}")
    private Duration timeout;

    @Bean
    public EmbeddingModel embeddingModel() {
        // 明确指定使用Spring RestClient作为HTTP客户端
        new SpringRestClientBuilderFactory()
                .create()
                .connectTimeout(timeout)
                .build();
        HttpClientBuilder httpClientBuilder = SpringRestClient.builder();

        return OllamaEmbeddingModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .timeout(timeout)
                .httpClientBuilder(httpClientBuilder)
                .build();
    }
}