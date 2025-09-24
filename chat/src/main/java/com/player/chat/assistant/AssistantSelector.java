// AssistantSelector.java
package com.player.chat.assistant;

import com.player.chat.entity.ChatParamsEntity;
import com.player.chat.mapper.ChatMapper;
import com.player.common.entity.ChatModelEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class AssistantSelector {

    private  ChatMapper chatMapper;

    public AssistantSelector(ChatMapper chatMapper) {
        this.chatMapper = chatMapper;
    }

    public Flux<String> selectAssistant(
            ChatParamsEntity chatParamsEntity,
            QwenAssistant qwenAssistant,
            DeepSeekAssistant deepSeekAssistant,
            DeepSeekOnlineAssistant deepSeekOnlineAssistant,
            QwenOnlineAssistant qwenOnlineAssistant
    ) {
        String language =  "zh".equals(chatParamsEntity.getLanguage()) ? "请用中文回答" : "Please respond in English";
        String prompt = chatParamsEntity.getShowThink() ? chatParamsEntity.getPrompt() : chatParamsEntity.getPrompt() + " /no_think";
        String chatId = chatParamsEntity.getChatId();
        ChatModelEntity chatModel = chatMapper.getModelById(chatParamsEntity.getModelId());
        if(chatModel.getType().equals("qwen_ollama")) {
            return qwenAssistant.chat(
                    chatId,
                    prompt,
                    language
            );
        } else if (chatModel.getType().equals("deepseek_ollama")) {
            return deepSeekAssistant.chat(
                    chatId,
                    prompt,
                    language
            );
        }else if(chatModel.getType().equals("deepseek_online")){
            return deepSeekOnlineAssistant.chat(
                    chatId,
                    prompt,
                    language
            );
        }else if(chatModel.getType().equals("qwen_online")) {
            return qwenOnlineAssistant.chat(
                    chatId,
                    prompt,
                    language
            );
        }
        return Flux.empty();
    }
}