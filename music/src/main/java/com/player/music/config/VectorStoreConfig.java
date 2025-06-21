package com.player.music.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class VectorStoreConfig {

    @Value("${spring.ai.ollama.api-url}")
    private String ollamaApiUrl;

    @Bean
    public EmbeddingModel embeddingModel(OllamaApi ollamaApi) {
        // 配置Ollama选项
        OllamaOptions options = OllamaOptions.builder()
                .withModel("mxbai-embed-large") // 指定模型名称
                .build();
        // 创建Ollama客户端
        OllamaClient ollamaClient = new OllamaClient(ollamaApiUrl);
        // 拉取模型
        ollamaClient.getModel("mxbai-embed-large");
        // 创建并返回OllamaEmbeddingModel实例
        return new OllamaEmbeddingModel(ollamaClient, options);
    }

    @Bean
    public VectorStore vectorStore(JedisPooled jedis, EmbeddingModel embeddingModel) {
        return RedisVectorStore.builder(jedis, embeddingModel).build();
    }
}