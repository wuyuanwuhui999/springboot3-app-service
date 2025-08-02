// AssistantSelector.java
package com.player.ai.assistant;

import com.player.ai.entity.ChatParamsEntity;
import reactor.core.publisher.Flux;

public class AssistantSelector {
    public static Flux<String> selectAssistant(
            ChatParamsEntity chatParamsEntity,
            QwenAssistant qwenAssistant,
            DeepSeekAssistant deepSeekAssistant
    ) {
        String language =  "zh".equals(chatParamsEntity.getLanguage()) ? "请用中文回答" : "Please respond in English";
        String prompt = chatParamsEntity.getShowThink() ? chatParamsEntity.getPrompt() : chatParamsEntity.getPrompt() + " /no_think";
        String chatId = chatParamsEntity.getChatId();
        if ("qwen3:8b".equals(chatParamsEntity.getModelName())) {
            return qwenAssistant.chat(
                    chatId,
                    prompt,
                    language
            );
        } else if ("deepseek-r1:8b".equals(chatParamsEntity.getModelName())) {
            return deepSeekAssistant.chat(
                    chatId,
                    prompt,
                    language
            );
        }
        return Flux.empty();
    }
}