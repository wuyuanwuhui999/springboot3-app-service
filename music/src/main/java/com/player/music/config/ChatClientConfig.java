package com.player.music.config;

import com.player.music.constants.SystemtConstants;
import com.player.music.tools.MusicTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }

    @Bean
    public ChatClient chatClient(OllamaChatModel model, RedisChatMemory redisChatMemory, MusicTool musicTool) {
        return ChatClient.builder(model)
                .defaultOptions(ChatOptions.builder()
                        .model("qwen3:8b")
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
