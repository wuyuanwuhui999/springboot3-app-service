package com.player.agent.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    @Qualifier("deepseekChatClient")
    public ChatClient deepseekChatClient() {
        OllamaApi ollamaApi = new OllamaApi("http://localhost:11434");

        OllamaOptions options = OllamaOptions.builder()
                .model("deepseek-r1:8b")
                .temperature(0.7)
                .build();

        // 使用 Builder 模式创建 OllamaChatModel
        OllamaChatModel chatModel = OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(options)
                .build(); // 使用默认的 toolCallingManager 和 observationRegistry

        return ChatClient.builder(chatModel).build();
    }

    @Bean
    @Qualifier("qwenChatClient")
    public ChatClient qwenChatClient() {
        OllamaApi ollamaApi = new OllamaApi("http://localhost:11434");

        OllamaOptions options = OllamaOptions.builder()
                .model("qwen3:8b")
                .temperature(0.7)
                .build();

        // 使用 Builder 模式创建 OllamaChatModel
        OllamaChatModel chatModel = OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(options)
                .build();

        return ChatClient.builder(chatModel).build();
    }
}