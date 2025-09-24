// AssistantSelector.java
package com.player.chat.assistant;

import com.player.chat.entity.ChatParamsEntity;
import com.player.chat.mapper.ChatMapper;
import com.player.common.entity.ChatModelEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
@Component
public class AssistantSelector {

    private final ChatMapper chatMapper;

    public AssistantSelector(ChatMapper chatMapper) {
        this.chatMapper = chatMapper;
    }

    public Flux<String> selectAssistant(
            ChatParamsEntity chatParamsEntity,
            QwenOllamaAssistant qwenOllamaAssistant,
            DeepSeekOllamaAssistant deepSeekOllamaAssistant,
            DeepSeekOnlineAssistant deepSeekOnlineAssistant,
            QwenOnlineAssistant qwenOnlineAssistant
    ) {
        String language = "zh".equals(chatParamsEntity.getLanguage()) ? "请用中文回答" : "Please respond in English";
        String prompt = chatParamsEntity.getShowThink() ? chatParamsEntity.getPrompt() : chatParamsEntity.getPrompt() + " /no_think";
        String chatId = chatParamsEntity.getChatId();
        ChatModelEntity chatModel = chatMapper.getModelById(chatParamsEntity.getModelId());

        if (chatModel == null || chatModel.getDisabled() == 1) {
            return Flux.error(new RuntimeException("所选模型不存在或已被禁用"));
        }

        String modelType = chatModel.getType();
        if (modelType.equals("qwen_ollama")) {
            return qwenOllamaAssistant.chat(chatId, prompt, language);
        } else if (modelType.equals("deepseek_ollama")) {
            return deepSeekOllamaAssistant.chat(chatId, prompt, language);
        } else if (modelType.equals("deepseek_online")) {
            return deepSeekOnlineAssistant.chat(chatId, prompt, language);
        } else if (modelType.equals("qwen_online")) {
            return qwenOnlineAssistant.chat(chatId, prompt, language);
        }

        return Flux.error(new RuntimeException("不支持的模型类型: " + modelType));
    }
}