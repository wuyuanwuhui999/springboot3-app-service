// AssistantSelector.java
package com.player.chat.assistant;

import com.player.chat.entity.ChatParamsEntity;
import com.player.chat.mapper.ChatMapper;
import com.player.common.entity.ChatModelEntity;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

@Component
public class AssistantSelector {

    private final ChatMapper chatMapper;
    private final ApplicationContext applicationContext;
    private final Map<String, String> assistantBeanMapping;

    public AssistantSelector(ChatMapper chatMapper, ApplicationContext applicationContext) {
        this.chatMapper = chatMapper;
        this.applicationContext = applicationContext;

        // 初始化模型类型到Bean名称的映射
        this.assistantBeanMapping = new HashMap<>();
        assistantBeanMapping.put("qwen_ollama", "qwenOllamaAssistant");
        assistantBeanMapping.put("deepseek_ollama", "deepSeekOllamaAssistant");
        assistantBeanMapping.put("deepseek_online", "deepSeekOnlineAssistant");
        assistantBeanMapping.put("qwen_online", "qwenOnlineAssistant");
    }

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

        String modelType = chatModel.getType();

        // 从映射中获取Bean名称
        String beanName = assistantBeanMapping.get(modelType);
        if (beanName == null) {
            return Flux.error(new RuntimeException("不支持的模型类型: " + modelType));
        }

        try {
            // 动态获取Bean
            Assistant assistant = (Assistant) applicationContext.getBean(beanName);
            return assistant.chat(chatId, prompt, language, systemPrompt);
        } catch (Exception e) {
            return Flux.error(new RuntimeException("获取模型服务失败: " + e.getMessage()));
        }
    }
}