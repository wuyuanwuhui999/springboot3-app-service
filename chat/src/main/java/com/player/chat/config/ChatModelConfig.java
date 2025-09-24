package com.player.chat.config;

import com.player.chat.mapper.ChatMapper;
import com.player.common.entity.ChatModelEntity;
import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilder;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class ChatModelConfig {

    private final ChatMapper chatMapper;

    public ChatModelConfig(ChatMapper chatMapper) {
        this.chatMapper = chatMapper;
    }

    /**
     * 获取可用的模型配置（未被禁用）
     */
    private ChatModelEntity getAvailableModelConfig(String modelType) {
        ChatModelEntity model = chatMapper.getModelByType(modelType);
        return (model != null && model.getDisabled() != null && model.getDisabled() == 0) ? model : null;
    }

    @Bean(name = "qwenStreamingChatModel")
    public OllamaStreamingChatModel qwenStreamingChatModel() {
        ChatModelEntity model = getAvailableModelConfig("qwen_ollama");
        if (model == null) {
            return null; // 不创建 Bean
        }

        Map<String, String> map = new HashMap<>();
        map.put("Content-Type", "application/json;charset=utf-8");

        return OllamaStreamingChatModel.builder()
                .baseUrl(model.getBaseUrl())
                .modelName(model.getModelName())
                .temperature(0.7)
                .timeout(Duration.ofMinutes(2))
                .customHeaders(map)
                .httpClientBuilder(new SpringRestClientBuilder())
                .build();
    }

    @Bean(name = "deepSeekStreamingChatModel")
    public OllamaStreamingChatModel deepSeekStreamingChatModel() {
        ChatModelEntity model = getAvailableModelConfig("deepseek_ollama");
        if (model == null) {
            return null;
        }

        Map<String, String> map = new HashMap<>();
        map.put("Content-Type", "application/json;charset=utf-8");

        return OllamaStreamingChatModel.builder()
                .baseUrl(model.getBaseUrl())
                .modelName(model.getModelName())
                .temperature(0.7)
                .timeout(Duration.ofMinutes(2))
                .customHeaders(map)
                .httpClientBuilder(new SpringRestClientBuilder())
                .build();
    }

    @Bean(name = "deepseekOnlineChatModel")
    public OpenAiStreamingChatModel deepseekOnlineChatModel() {
        ChatModelEntity model = getAvailableModelConfig("deepseek_online");
        if (model == null) {
            return null;
        }

        return OpenAiStreamingChatModel.builder()
                .baseUrl(model.getBaseUrl())
                .apiKey(model.getApiKey())
                .modelName(model.getModelName())
                .timeout(Duration.ofMinutes(2))
                .httpClientBuilder(new SpringRestClientBuilder())
                .build();
    }

    @Bean(name = "qwenOnlineChatModel")
    public OpenAiStreamingChatModel qwenOnlineChatModel() {
        ChatModelEntity model = getAvailableModelConfig("qwen_online");
        if (model == null) {
            return null;
        }
        return OpenAiStreamingChatModel.builder()
                .baseUrl(model.getBaseUrl())
                .apiKey(model.getApiKey())
                .modelName(model.getModelName())
                .timeout(Duration.ofMinutes(2))
                .httpClientBuilder(new SpringRestClientBuilder())
                .build();
    }
}