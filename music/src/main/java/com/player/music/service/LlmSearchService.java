package com.player.music.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.player.common.constants.MusicSqlPrompt;
import com.player.common.entity.ChatModelEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 智能搜索用的大模型调用（NL2SQL）。
 * <p>
 * 实现与 FastAPI 项目 common/services/llm_service.py 的 generate_music_sql 对齐：
 * 提示词来自 common 的 {@link MusicSqlPrompt}，返回解析后的 JSON（含 sql_condition）；
 * 任何失败（模型创建失败、调用异常、返回不是 JSON）都返回 null，由调用方降级为普通搜索。
 */
@Slf4j
@Service
public class LlmSearchService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 让大模型把自然语言转成音乐查询条件。
     *
     * @param modelConfig chat_model 表里的模型配置
     * @param userId      当前用户ID（写进提示词）
     * @param keyword     搜索关键词
     * @return 解析后的意图 Map（action / search_type / search_keyword / sql_condition / explanation），
     *         失败返回 null
     */
    public Map<String, Object> generateMusicSql(ChatModelEntity modelConfig, String userId, String keyword) {
        if (modelConfig == null) {
            return null;
        }
        try {
            ChatModel chatModel = buildChatModel(modelConfig);
            if (chatModel == null) {
                log.warn("[LlmSearch] 模型不可用: type={}, model={}", modelConfig.getType(), modelConfig.getModelName());
                return null;
            }

            ChatClient chatClient = ChatClient.builder(chatModel).build();
            String text = chatClient.prompt()
                    .system(MusicSqlPrompt.sqlGeneratorPrompt(userId))
                    .user("用户输入: " + keyword)
                    .call()
                    .content();

            if (!StringUtils.hasText(text)) {
                log.warn("[LlmSearch] 大模型返回为空");
                return null;
            }

            String json = stripCodeFence(text);
            @SuppressWarnings("unchecked")
            Map<String, Object> result = OBJECT_MAPPER.readValue(json, Map.class);
            log.info("[LlmSearch] NL2SQL 结果: {}", result);
            return result;
        } catch (Exception e) {
            log.error("[LlmSearch] NL2SQL 失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /** 按 chat_model 的 type 构建聊天模型（ollama / online），与 agent 模块 ChatClientConfig 一致 */
    private ChatModel buildChatModel(ChatModelEntity modelConfig) {
        String baseUrl = modelConfig.getBaseUrl();
        String modelName = modelConfig.getModelName();

        if ("ollama".equals(modelConfig.getType())) {
            OllamaApi ollamaApi = OllamaApi.builder().baseUrl(baseUrl).build();
            OllamaChatOptions options = OllamaChatOptions.builder()
                    .model(modelName)
                    .temperature(0.7)
                    .build();
            return OllamaChatModel.builder().ollamaApi(ollamaApi).defaultOptions(options).build();
        }

        if ("online".equals(modelConfig.getType())) {
            OpenAiApi openAiApi = OpenAiApi.builder()
                    .baseUrl(baseUrl)
                    .apiKey(modelConfig.getApiKey())
                    .build();
            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .model(modelName)
                    .temperature(0.7)
                    .build();
            return OpenAiChatModel.builder().openAiApi(openAiApi).defaultOptions(options).build();
        }

        log.error("[LlmSearch] 不支持的模型类型: {}", modelConfig.getType());
        return null;
    }

    /** 去掉大模型返回里的 ```json ... ``` 包裹 */
    private String stripCodeFence(String text) {
        String t = text.trim();
        if (t.contains("```json")) {
            return t.split("```json")[1].split("```")[0].trim();
        }
        if (t.contains("```")) {
            return t.split("```")[1].split("```")[0].trim();
        }
        return t;
    }

    /** 方便调用方构造空结果 */
    public static Map<String, Object> emptyResult() {
        return new HashMap<>();
    }
}
