package com.player.music.config;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;

@Configuration
public class VectorStoreConfig {
    @Value("${spring.ai.ollama.base-url}")
    private String ollamaBaseUrl;

    @Value("${spring.ai.ollama.embedding.model}")
    private String embeddingModelName;

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
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel)
                .build();
        // 尝试从文件加载已有数据
        try {
            vectorStore.load(new File("G:\\static\\ai\\vector_store.json"));
        } catch (Exception e) {
            System.out.println("No existing vector store file found, starting fresh");
        }

        // 添加关闭钩子以保存数据
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                vectorStore.save(new File("G:\\static\\ai\\vector_store.json"));
                System.out.println("Vector store data saved successfully");
            } catch (Exception e) {
                System.err.println("Failed to save vector store data: " + e.getMessage());
            }
        }));

        return vectorStore;
    }
}