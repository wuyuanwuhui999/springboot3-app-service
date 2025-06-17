package com.player.music.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

@Configuration
public class VectorStoreConfig {
    @Bean
    public VectorStore vectorStore(JedisPooled jedis, EmbeddingModel embeddingModel){
        return RedisVectorStore.builder(jedis,embeddingModel).build();
    }
}
