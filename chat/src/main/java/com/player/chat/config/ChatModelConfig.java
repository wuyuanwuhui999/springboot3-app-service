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
     * 获取模型配置
     */
    private ChatModelEntity getModelConfig(String modelType) {
        return chatMapper.getModelByType(modelType);
    }

    @Bean(name = "qwenStreamingChatModel")
    public OllamaStreamingChatModel qwenStreamingChatModel() {
        ChatModelEntity model = getModelConfig("qwen_ollama");
        if (model == null) {
            throw new RuntimeException("Qwen Ollama模型配置未找到");
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
        ChatModelEntity model = getModelConfig("deepseek_ollama");
        if (model == null) {
            throw new RuntimeException("DeepSeek Ollama模型配置未找到");
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
        ChatModelEntity model = getModelConfig("deepseek_online");
        if (model == null) {
            throw new RuntimeException("DeepSeek在线模型配置未找到");
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
        ChatModelEntity model = getModelConfig("qwen_online");
        if (model == null) {
            throw new RuntimeException("Qwen在线模型配置未找到");
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