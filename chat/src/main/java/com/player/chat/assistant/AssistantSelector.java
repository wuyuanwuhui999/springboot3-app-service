package com.player.chat.assistant;

import com.player.chat.assistant.impl.DeepSeekOnlineAssistant;
import com.player.chat.assistant.impl.TongyiOnlineAssistant;
import com.player.chat.entity.ChatParamsEntity;
import com.player.chat.mapper.ChatMapper;
import com.player.common.entity.ChatModelEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Slf4j
@Component
public class AssistantSelector {

    @Autowired
    private ChatMapper chatMapper;

    @Autowired
    private QwenAssistant qwenAssistant;

    @Autowired
    private DeepSeekAssistant deepSeekAssistant;

    @Autowired
    private DeepSeekOnlineAssistant deepSeekOnlineAssistant;

    @Autowired
    private TongyiOnlineAssistant tongyiOnlineAssistant;

    public Flux<String> selectAssistant(ChatParamsEntity chatParamsEntity) {
        String modelId = chatParamsEntity.getModelId(); // 改为使用modelId
        String language = "zh".equals(chatParamsEntity.getLanguage()) ? "请用中文回答" : "Please respond in English";
        String prompt = chatParamsEntity.getShowThink() ? chatParamsEntity.getPrompt() : chatParamsEntity.getPrompt() + " /no_think";
        String chatId = chatParamsEntity.getChatId();

        // 根据modelId查询模型配置
        ChatModelEntity modelConfig = chatMapper.getModelById(modelId);
        if (modelConfig == null) {
            return Flux.error(new RuntimeException("模型配置不存在或已被禁用"));
        }

        log.info("使用模型: {} - {}", modelConfig.getType(), modelConfig.getModelName());

        // 根据模型类型选择不同的实现
        switch (modelConfig.getType().toLowerCase()) {
            case "ollama":
                return selectOllamaModel(modelConfig, chatId, prompt, language);
            case "deepseek":
                return deepSeekOnlineAssistant.chat(chatId, prompt, language,
                        modelConfig.getApiKey(), modelConfig.getBaseUrl());
            case "tongyi":
                return tongyiOnlineAssistant.chat(chatId, prompt, language,
                        modelConfig.getApiKey(), modelConfig.getBaseUrl());
            default:
                return Flux.error(new RuntimeException("不支持的模型类型: " + modelConfig.getType()));
        }
    }

    private Flux<String> selectOllamaModel(ChatModelEntity modelConfig, String chatId, String prompt, String language) {
        // 对于ollama类型，根据模型名称选择具体的本地模型
        String modelName = modelConfig.getModelName();
        if ("qwen".equalsIgnoreCase(modelName) || modelName.contains("qwen")) {
            return qwenAssistant.chat(chatId, prompt, language);
        } else if ("deepseek".equalsIgnoreCase(modelName) || modelName.contains("deepseek")) {
            return deepSeekAssistant.chat(chatId, prompt, language);
        } else {
            // 默认使用qwen
            return qwenAssistant.chat(chatId, prompt, language);
        }
    }
}