package com.player.chat.config;

import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wuwenqiang
 * @description: 向量数据库配置 - 修复Chroma连接405错误
 * @date: 2026-07-01 22:45
 */
@Configuration
public class VectorStoreConfig {

    @Value("${chroma.base-url}")
    private String chromaUrl;

    @Value("${chroma.collection-name:chat_vector_collection}")
    private String collectionName;

    /**
     * 配置Chroma向量存储
     * 修复点：显式构建ChromaClient并指定API路径，避免405 Method Not Allowed错误
     */
    @Bean
    public ChromaEmbeddingStore chromaEmbeddingStore() {
        // 2. 显式构建ChromaClient，避免LangChain4j内部默认路径解析错误
        // 使用Builder模式替代直接new，提高可读性和兼容性
        return ChromaEmbeddingStore.builder()
                .baseUrl(chromaUrl) // 使用修正后的URL
                .collectionName(collectionName)
                .build();
    }
}