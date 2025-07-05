package com.player.music.config;

import com.player.music.constants.SystemtConstants;
import com.player.music.tools.MusicTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Slf4j
@Configuration
public class ChatClientConfig {

    @Value("${spring.ai.ollama.chat.qwen-model}")
    private String qwenModel;

    @Value("${spring.ai.ollama.chat.deepseek-model}")
    private String deepseekModel;

    @Bean(name = "qwenChatClient")
    public ChatClient qwenChatClient(OllamaChatModel model, RedisChatMemory redisChatMemory, MusicTool musicTool) {
        log.info("创建Qwen聊天客户端，模型: {}", model.getDefaultOptions().getModel());
        return ChatClient.builder(model)
                .defaultOptions(ChatOptions.builder()
                        .model(qwenModel)
                        .build())
                .defaultSystem(SystemtConstants.MUSIC_SYSTEMT_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        new MessageChatMemoryAdvisor(redisChatMemory)
                )
                .defaultTools(musicTool)
                .build();
    }

    @Bean(name = "deepseekChatClient")
    public ChatClient deepseekChatClient(OllamaChatModel model, RedisChatMemory redisChatMemory, MusicTool musicTool) {
        log.info("创建DeepSeek聊天客户端，模型: {}", model.getDefaultOptions().getModel());
        return ChatClient.builder(model)
                .defaultOptions(ChatOptions.builder()
                        .model(deepseekModel)
                        .build())
                .defaultSystem(SystemtConstants.MUSIC_SYSTEMT_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        new MessageChatMemoryAdvisor(redisChatMemory)
                )
                .defaultTools(musicTool)
                .build();
    }
}
