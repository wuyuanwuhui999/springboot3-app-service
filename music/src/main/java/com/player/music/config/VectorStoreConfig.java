package com.player.music.config;

import io.micrometer.observation.ObservationRegistry;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStoreOptions;
import org.springframework.ai.vectorstore.elasticsearch.SimilarityFunction;
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

    @Value("${spring.ai.vectorstore.elasticsearch.index-name}")
    private String IndexName;

    @Value("${spring.elasticsearch.port}")
    private int port;

    @Value("${spring.elasticsearch.host}")
    private String host;

    @Value("${spring.ai.vectorstore.elasticsearch.dimensions}")
    private int dimensions;

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
    public RestClient restClient() {
        return RestClient.builder(new HttpHost(host, port, "http"))
                .build();
    }

    @Bean
    public VectorStore vectorStore(RestClient restClient, EmbeddingModel embeddingModel) {
        ElasticsearchVectorStoreOptions options = new ElasticsearchVectorStoreOptions();
        options.setIndexName(IndexName);    // Optional: defaults to "spring-ai-document-index"
        options.setSimilarity(SimilarityFunction.cosine);           // Optional: defaults to COSINE
        options.setDimensions(dimensions);             // Optional: defaults to model dimensions or 1536

        return new ElasticsearchUserAwareVectorStore(restClient, embeddingModel, options);
    }
}