package com.player.chat.config;

import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class OllamaConfig {
    @Value("${langchain4j.ollama.chat-model.base-url}")
    private String url;

    @Value("${langchain4j.ollama.chat-model.qwen-model-name}")
    private String qwenModelName;

    @Value("${langchain4j.ollama.chat-model.deepseek-model-name}")
    private String deepSeekModelName;

    @Value("${langchain4j.ollama.chat-model.temperature}")
    private Double temperature;

    @Bean(name = "qwenStreamingChatModel")
    public OllamaStreamingChatModel qwenStreamingChatModel() {
        Map<String, String> map = new HashMap();
        map.put("Content-Type", "application/json;charset=utf-8");
        return OllamaStreamingChatModel.builder()
                .baseUrl(url)
                .modelName(qwenModelName)
                .temperature(temperature)
                .timeout(Duration.ofMinutes(2))
                .customHeaders(map)
                .build();
    }

    @Bean(name = "deepSeekStreamingChatModel")
    public OllamaStreamingChatModel deepSeekStreamingChatModel() {
        Map<String, String> map = new HashMap();
        map.put("Content-Type", "application/json;charset=utf-8");
        return OllamaStreamingChatModel.builder()
                .baseUrl(url)
                .modelName(deepSeekModelName)
                .temperature(temperature)
                .timeout(Duration.ofMinutes(2))
                .customHeaders(map)
                .build();
    }
}