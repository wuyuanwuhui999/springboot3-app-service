package com.player.chat.utils;

import com.player.common.entity.ChatModelEntity;
import dev.langchain4j.http.client.spring.restclient.SpringRestClient;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 聊天模型工厂：按模型表（chat_model）的 type / base_url / api_key 构建 langchain4j 的同步 ChatModel。
 * <p>
 * 与 AssistantSelector 的流式模型同源，供「会话压缩总结」「智能分割」这类非流式场景复用，
 * 对应 FastAPI 项目 chat_service.py 里的 _create_chat_model。
 */
public final class ChatModelFactory {

    private ChatModelFactory() {
    }

    public static ChatModel create(ChatModelEntity chatModelEntity) {
        return create(chatModelEntity, 0.3);
    }

    public static ChatModel create(ChatModelEntity chatModelEntity, double temperature) {
        if ("ollama".equals(chatModelEntity.getType())) {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json;charset=utf-8");
            return OllamaChatModel.builder()
                    .baseUrl(chatModelEntity.getBaseUrl())
                    .modelName(chatModelEntity.getModelName())
                    .temperature(temperature)
                    .timeout(Duration.ofMinutes(2))
                    .customHeaders(headers)
                    .httpClientBuilder(SpringRestClient.builder())
                    .build();
        }
        return OpenAiChatModel.builder()
                .baseUrl(chatModelEntity.getBaseUrl())
                .apiKey(chatModelEntity.getApiKey())
                .modelName(chatModelEntity.getModelName())
                .temperature(temperature)
                .timeout(Duration.ofMinutes(2))
                .httpClientBuilder(SpringRestClient.builder())
                .build();
    }
}
