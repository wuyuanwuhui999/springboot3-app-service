// AssistantSelector.java
package com.player.gateway.chat.assistant;

import com.player.gateway.chat.entity.ChatParamsEntity;
import com.player.gateway.chat.mapper.ChatMapper;
import com.player.gateway.common.entity.ChatModelEntity;
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

        // 获取动态提示词，如果为空则使用固定提示词
        String systemPrompt = chatParamsEntity.getSystemPrompt();
        if (systemPrompt == null || systemPrompt.trim().isEmpty()) {
            systemPrompt = "你叫小吴同学，是一个无所不能的AI助手，上知天文下知地理，请用小吴同学的身份回答问题。\n{{language}}";
        }


        String modelType = chatModel.getType();
        if (modelType.equals("qwen_ollama")) {
            return qwenOllamaAssistant.chat(chatId, prompt,systemPrompt, language);
        } else if (modelType.equals("deepseek_ollama")) {
            return deepSeekOllamaAssistant.chat(chatId, prompt,systemPrompt, language);
        } else if (modelType.equals("deepseek_online")) {
            return deepSeekOnlineAssistant.chat(chatId, prompt,systemPrompt, language);
        } else if (modelType.equals("qwen_online")) {
            return qwenOnlineAssistant.chat(chatId, prompt,systemPrompt, language);
        }

        return Flux.error(new RuntimeException("不支持的模型类型: " + modelType));
    }
}