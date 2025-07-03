package com.player.ai.assistant;

import reactor.core.publisher.Flux;

public class AssistantSelector {
    public static Flux<String> selectAssistant(
            String modelName,
            QwenAssistant qwenAssistant,
            DeepSeekAssistant deepSeekAssistant,
            String chatId,
            String prompt
    ) {
        if ("qwen3:8b".equals(modelName)) {
            return qwenAssistant.chat(chatId, prompt);
        } else if ("deepseek-r1:8b".equals(modelName)) {
            return deepSeekAssistant.chat(chatId, prompt);
        }
        return Flux.empty();
    }
}