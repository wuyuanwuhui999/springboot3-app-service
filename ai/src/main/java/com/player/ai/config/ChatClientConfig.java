package com.player.ai.config;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.chat.client.ChatClient;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient() {
        // 根据实际需求初始化和返回ChatClient实例
        return new ChatClient() {
            @Override
            public ChatClientRequestSpec prompt() {
                return null;
            }

            @Override
            public ChatClientRequestSpec prompt(String content) {
                return null;
            }

            @Override
            public ChatClientRequestSpec prompt(Prompt prompt) {
                return null;
            }

            @Override
            public Builder mutate() {
                return null;
            }
        };
    }
}
