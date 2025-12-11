package com.player.agent.config;

import io.micrometer.observation.ObservationRegistry;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.ollama.api.OllamaEmbeddingOptions;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStore;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStoreOptions;
import org.springframework.ai.vectorstore.elasticsearch.SimilarityFunction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class VectorStoreConfig {
    @Value("${spring.ai.ollama.base-url}")
    private String ollamaBaseUrl;

    @Value("${spring.ai.ollama.embedding.model}")
    private String embeddingModelName;

    @Value("${spring.ai.vectorstore.elasticsearch.index-name}")
    private String IndexName;

    @Value("${spring.elasticsearch.port}")
    private int port;

    @Value("${spring.elasticsearch.host}")
    private String host;

    @Value("${spring.ai.vectorstore.elasticsearch.dimensions}")
    private int dimensions;

    @Lazy
    @Bean
    public EmbeddingModel embeddingModel() {
        OllamaEmbeddingOptions ollamaEmbeddingOptions = OllamaEmbeddingOptions.builder().model(embeddingModelName).build();
        OllamaApi ollamaApi1 = OllamaApi.builder().baseUrl(ollamaBaseUrl).build();
        return OllamaEmbeddingModel.builder().ollamaApi(ollamaApi1).defaultOptions(ollamaEmbeddingOptions).build();
    }

    @Lazy
    @Bean
    public RestClient restClient() {
        return RestClient.builder(new HttpHost(host, port, "http"))
                .build();
    }

    @Lazy
    @Bean
    public VectorStore vectorStore(RestClient restClient, EmbeddingModel embeddingModel) {
        ElasticsearchVectorStoreOptions options = new ElasticsearchVectorStoreOptions();
        options.setIndexName(IndexName);    // Optional: defaults to "spring-ai-document-index"
        options.setSimilarity(SimilarityFunction.cosine);           // Optional: defaults to COSINE
        options.setDimensions(dimensions);             // Optional: defaults to model dimensions or 1536
        return ElasticsearchVectorStore.builder(restClient, embeddingModel)
                .options(options)
                .initializeSchema(true).build();
    }
}