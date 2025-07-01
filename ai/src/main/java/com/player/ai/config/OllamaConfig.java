package com.player.ai.config;

import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;

//@Configuration
//public class OllamaConfig {
//    @Value("langchain4j.ollama.chat-model.base-url")
//    private String url;
//
//    @Value("langchain4j.ollama.chat-model.model-name")
//    private String modelName;
//
//    @Bean
//    public OllamaStreamingChatModel streamingChatModel() {
//        return OllamaStreamingChatModel.builder()
//                .baseUrl(url)
//                .modelName(modelName)
//                .timeout(Duration.ofMinutes(2))
//                .build();
//    }
//}
@Configuration
public class OllamaConfig {
    @Value("${langchain4j.ollama.chat-model.base-url}")
    private String url;

    @Value("${langchain4j.ollama.chat-model.model-name}")
    private String modelName;

    @Bean
    public OllamaStreamingChatModel streamingChatModel() {
        return OllamaStreamingChatModel.builder()
                .baseUrl(url)
                .modelName(modelName)
                .timeout(Duration.ofMinutes(2))
                .build();
    }
}