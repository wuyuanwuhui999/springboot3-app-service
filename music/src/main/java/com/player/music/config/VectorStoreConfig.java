package com.player.music.config;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chroma.vectorstore.ChromaApi;
import org.springframework.ai.chroma.vectorstore.ChromaVectorStore;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class VectorStoreConfig {
    @Value("${spring.ai.ollama.base-url}")
    private String ollamaBaseUrl;

    @Value("${spring.ai.ollama.embedding.model}")
    private String embeddingModelName;

    @Value("${chroma.host}")
    private String chromaHost;

    @Value("${chroma.collectName}")
    private String collectName;

    @Bean
    public OllamaApi ollamaApi() {
        return new OllamaApi(ollamaBaseUrl);
    }

    @Bean
    public EmbeddingModel embeddingModel(OllamaApi ollamaApi) {
        OllamaOptions options = OllamaOptions.builder()
                .model(embeddingModelName)
                .build();
        return new OllamaEmbeddingModel(ollamaApi,options,ObservationRegistry.create(), ModelManagementOptions.builder().build());
    }

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        // 创建Chroma API客户端
        ChromaApi chromaApi = new ChromaApi(chromaHost);
        return new DynamicVectorStore(chromaApi, embeddingModel, collectName);
    }
}