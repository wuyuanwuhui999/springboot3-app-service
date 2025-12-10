package com.player.agent.config;

import com.player.agent.constants.SystemtConstants;
import com.player.agent.mapper.AgentMapper;
import com.player.common.entity.ChatModelEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.model.ApiKey;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Slf4j
@Configuration
public class ChatClientConfig {

    @Value("${spring.ai.ollama.chat.qwen-model}")
    private String qwenModel;

    @Value("${spring.ai.ollama.chat.deepseek-model}")
    private String deepseekModel;
    
    @Autowired
    private AgentMapper agentMapper;

    @Lazy
    @Bean(name = "qwenOllamaChatClient")
    public ChatClient qwenOllamaChatClient(OllamaChatModel model, RedisChatMemory redisChatMemory) {
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
                .build();
    }

    @Lazy
    @Bean(name = "deepseekOllamaChatClient")
    public ChatClient deepseekOllamaChatClient(OllamaChatModel model, RedisChatMemory redisChatMemory) {
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
                .build();
    }

    @Lazy
    @Bean(name = "deepseekOnlineChatClient")
    public ChatClient deepseekOnlineChatClient(RedisChatMemory redisChatMemory) {
        log.info("创建deepseek聊天客户端，模型: deepseek-chat");
        return getChatClient("deepseek_online",redisChatMemory);
    }

    @Lazy
    @Bean(name = "qwenOnlineChatClient")
    public ChatClient qwenOnlineChatClient(RedisChatMemory redisChatMemory) {
        log.info("创建qwen聊天客户端，模型: qwen3");
        return getChatClient("qwen_online",redisChatMemory);
    }

    private ChatClient getChatClient(String modelType,RedisChatMemory redisChatMemory){
        ChatModelEntity deepseekOnlineModel = agentMapper.getModelByType(modelType);// 从数据库中获取模型配置
        OpenAiApi openAiApi = new OpenAiApi(deepseekOnlineModel.getBaseUrl(), deepseekOnlineModel.getApiKey());
        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                .model(deepseekOnlineModel.getModelName())
                .temperature(0.7)
                .build();
        OpenAiChatModel chatModel = new OpenAiChatModel(openAiApi, chatOptions);
        return ChatClient.builder(chatModel)
                .defaultSystem(SystemtConstants.MUSIC_SYSTEMT_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        new MessageChatMemoryAdvisor(redisChatMemory)
                )
                .build();
    }
}
