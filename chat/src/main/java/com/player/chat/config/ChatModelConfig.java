package com.player.chat.config;

import com.player.chat.mapper.ChatMapper;
import com.player.common.entity.ChatModelEntity;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

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
        // 这里需要根据模型类型获取配置，您可能需要修改ChatMapper添加按类型查询的方法
        // 或者根据您的业务逻辑调整
        return chatMapper.getModelByType(modelType);
    }

    @Bean(name = "qwenStreamingChatModel")
    @Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
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
                .temperature(0.7) // 可以配置化或从数据库获取
                .timeout(Duration.ofMinutes(2))
                .customHeaders(map)
                .build();
    }

    @Bean(name = "deepSeekStreamingChatModel")
    @Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
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
                .temperature(0.7) // 可以配置化或从数据库获取
                .timeout(Duration.ofMinutes(2))
                .customHeaders(map)
                .build();
    }

    // 在ChatModelConfig中替换deepseekOnlineChatModel和qwenOnlineChatModel的定义
    @Bean(name = "deepseekOnlineStreamingChatModel")
    @Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public OpenAiStreamingChatModel deepseekOnlineStreamingChatModel() {
        ChatModelEntity model = getModelConfig("deepseek_online");
        if (model == null) {
            throw new RuntimeException("DeepSeek在线模型配置未找到");
        }

        return OpenAiStreamingChatModel.builder()
                .baseUrl(model.getBaseUrl())
                .apiKey(model.getApiKey())
                .modelName(model.getModelName())
                .timeout(Duration.ofMinutes(2))
                .build();
    }

    @Bean(name = "qwenOnlineStreamingChatModel")
    @Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public OpenAiStreamingChatModel qwenOnlineStreamingChatModel() {
        ChatModelEntity model = getModelConfig("qwen_online");
        if (model == null) {
            throw new RuntimeException("Qwen在线模型配置未找到");
        }

        return OpenAiStreamingChatModel.builder()
                .baseUrl(model.getBaseUrl())
                .apiKey(model.getApiKey())
                .modelName(model.getModelName())
                .timeout(Duration.ofMinutes(2))
                .build();
    }

}