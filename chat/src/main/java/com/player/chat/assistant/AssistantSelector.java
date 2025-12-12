// AssistantSelector.java
package com.player.chat.assistant;

import com.player.chat.entity.ChatParamsEntity;
import com.player.chat.mapper.ChatMapper;
import com.player.common.entity.ChatModelEntity;
import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilder;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
public class AssistantSelector {

    @Autowired
    private ChatMapper chatMapper;

    @Autowired
    private ChatMemoryProvider chatMemoryProvider;

    public Flux<String> selectAssistant(ChatParamsEntity chatParamsEntity) {
        String language = "zh".equals(chatParamsEntity.getLanguage()) ? "请用中文回答" : "Please respond in English";
        String prompt = chatParamsEntity.getShowThink() ? chatParamsEntity.getPrompt() : chatParamsEntity.getPrompt() + " /no_think";
        String chatId = chatParamsEntity.getChatId();

        ChatModelEntity chatModel = chatMapper.getModelById(chatParamsEntity.getModelId());

        if (chatModel == null || chatModel.getDisabled() == 1) {
            return Flux.error(new RuntimeException("所选模型不存在或已被禁用"));
        }

        // 获取动态提示词，如果为空则使用固定提示词
        String systemPrompt = chatParamsEntity.getSystemPrompt();
        if (systemPrompt == null || systemPrompt.trim().isEmpty()) {
            systemPrompt = "你叫小吴同学，是一个无所不能的AI助手，上知天文下知地理，请用小吴同学的身份回答问题。\n" + language;
        }else{
            systemPrompt += "\n" + language;
        }

        try {
            Assistant assistant = AiServices.builder(Assistant.class)
                    .streamingChatModel(getStreamingChatModel(chatModel))
                    .chatMemoryProvider(chatMemoryProvider)
                    .build();
            return assistant.chat(chatId, prompt, language, systemPrompt);
        } catch (Exception e) {
            return Flux.error(new RuntimeException("获取模型服务失败: " + e.getMessage()));
        }
    }

    private StreamingChatModel getStreamingChatModel(ChatModelEntity chatModelEntity){
        if("ollama".equals(chatModelEntity.getType())){
            Map<String, String> map = new HashMap<>();
            map.put("Content-Type", "application/json;charset=utf-8");

            return OllamaStreamingChatModel.builder()
                    .baseUrl(chatModelEntity.getBaseUrl())
                    .modelName(chatModelEntity.getModelName())
                    .temperature(0.7)
                    .timeout(Duration.ofMinutes(2))
                    .customHeaders(map)
                    .httpClientBuilder(new SpringRestClientBuilder())
                    .build();
        }else{
            return OpenAiStreamingChatModel.builder()
                    .baseUrl(chatModelEntity.getBaseUrl())
                    .apiKey(chatModelEntity.getApiKey())
                    .modelName(chatModelEntity.getModelName())
                    .timeout(Duration.ofMinutes(2))
                    .httpClientBuilder(new SpringRestClientBuilder())
                    .build();
        }
    }
}