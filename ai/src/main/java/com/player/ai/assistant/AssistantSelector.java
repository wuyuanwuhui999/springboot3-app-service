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
        if ("qwen3:8b".equals(chatParamsEntity.getModelName())) {
            return qwenAssistant.chat(
                    chatParamsEntity.getChatId(),
                    chatParamsEntity.getPrompt(),
                    chatParamsEntity.getShowThink(),
                    chatParamsEntity.getLanguage()
            );
        } else if ("deepseek-r1:8b".equals(chatParamsEntity.getModelName())) {
            return deepSeekAssistant.chat(
                    chatParamsEntity.getChatId(),
                    chatParamsEntity.getPrompt(),
                    chatParamsEntity.getShowThink(),
                    chatParamsEntity.getLanguage()
            );
        }
        return Flux.empty();
    }
}