package com.player.ai.config;

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
        return OllamaEmbeddingModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .timeout(timeout)
                .build();
    }
}
