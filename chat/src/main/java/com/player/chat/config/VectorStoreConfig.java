package com.player.chat.config;

import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.http.client.spring.restclient.SpringRestClient;
import dev.langchain4j.store.embedding.chroma.ChromaApiVersion;
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
        // 显式指定 SpringRestClient，避免 classpath 上多个 HTTP client 实现冲突
        //（langchain4j-open-ai 1.20.0 引入 jdk http client，与 spring-restclient 冲突）
        // 使用 V2 API（chromadb 0.6.3 的 V1 collection 查询接口有 bug，V2 正常）
        HttpClientBuilder httpClientBuilder = SpringRestClient.builder();
        return ChromaEmbeddingStore.builder()
                .apiVersion(ChromaApiVersion.V2)
                .baseUrl(chromaUrl) // 使用修正后的URL
                .tenantName("default_tenant")
                .databaseName("default_database")
                .collectionName(collectionName)
                .httpClientBuilder(httpClientBuilder)
                .build();
    }
}