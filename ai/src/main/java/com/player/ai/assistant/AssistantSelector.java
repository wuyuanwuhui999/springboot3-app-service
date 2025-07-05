package com.player.ai.assistant;

import reactor.core.publisher.Flux;

public class AssistantSelector {
    public static Flux<String> selectAssistant(
            String modelName,
            QwenAssistant qwenAssistant,
            DeepSeekAssistant deepSeekAssistant,
            String chatId,
            String prompt,
            boolean showThink
    ) {
        if ("qwen3:8b".equals(modelName)) {
            return qwenAssistant.chat(chatId, prompt, showThink);
        } else if ("deepseek-r1:8b".equals(modelName)) {
            return deepSeekAssistant.chat(chatId, prompt, showThink);
        }
        return Flux.empty();
    }
}