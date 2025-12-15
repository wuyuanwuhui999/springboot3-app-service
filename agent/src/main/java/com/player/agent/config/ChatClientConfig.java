package com.player.agent.config;

import com.player.agent.constants.SystemtConstants;
import com.player.agent.mapper.AgentMapper;
import com.player.common.entity.ChatModelEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

        log.info("创建ChatClient - 模型ID: {}, 模型名称: {}, 类型: {}",
                modelId, modelName, chatModelEntity.getType());

        if ("ollama".equals(chatModelEntity.getType())) {
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

        // 构建ChatClient，可选地使用聊天记忆
        ChatClient.Builder builder = ChatClient.builder(chatModel)
                .defaultSystem(SystemtConstants.SQL_GENERATOR_PROMPT)
                .defaultAdvisors(new SimpleLoggerAdvisor());

        // 如果聊天记忆可用，添加它
        if (redisChatMemory != null) {
            try {
                builder.defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(redisChatMemory).build()
                );
                log.debug("已启用聊天记忆功能");
            } catch (Exception e) {
                log.warn("启用聊天记忆失败，将继续无记忆模式", e);
            }
        } else {
            log.debug("未启用聊天记忆功能");
        }

        return builder.build();
    }
}