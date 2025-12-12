package com.player.agent.config;

import com.player.agent.constants.SystemtConstants;
import com.player.agent.mapper.AgentMapper;
import com.player.common.entity.ChatModelEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.model.ApiKey;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChatClientConfig {

    @Value("${spring.ai.ollama.chat.qwen-model}")
    private String qwenModel;

    @Value("${spring.ai.ollama.chat.deepseek-model}")
    private String deepseekModel;

    @Autowired
    private AgentMapper agentMapper;

    public ChatClient getChatClient(String modelId, RedisChatMemory redisChatMemory) {
        ChatModelEntity chatModelEntity = agentMapper.getModelById(modelId);
        ChatModel chatModel;
        String baseUrl = chatModelEntity.getBaseUrl();
        String modelName = chatModelEntity.getModelName();
        if (chatModelEntity.getType().contains("ollama")) {
            OllamaApi ollamaApi = OllamaApi.builder()
                    .baseUrl(baseUrl)
                    .build();
            OllamaChatOptions ollamaChatOptions = OllamaChatOptions.builder()
                    .model(modelName)
                    .temperature(0.7)
                    .build();
            chatModel = OllamaChatModel.builder()
                    .ollamaApi(ollamaApi)
                    .defaultOptions(ollamaChatOptions)
                    .build();
        } else {
            OpenAiApi openAiApi = OpenAiApi.builder()
                    .baseUrl(baseUrl)
                    .apiKey(chatModelEntity.getApiKey())
                    .build();
            OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder()
                    .model(modelName)
                    .temperature(0.7)
                    .build();
            chatModel = OpenAiChatModel.builder()
                    .openAiApi(openAiApi)
                    .defaultOptions(openAiChatOptions)
                    .build();
        }
        return ChatClient.builder(chatModel)
                .defaultSystem(SystemtConstants.MUSIC_SYSTEMT_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(redisChatMemory).build()
                )
                .build();
    }
}
