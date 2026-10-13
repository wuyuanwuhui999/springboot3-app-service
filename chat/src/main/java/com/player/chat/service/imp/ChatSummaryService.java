package com.player.chat.service.imp;

import com.player.chat.config.MongoChatMemoryStore;
import com.player.chat.constants.SystemtConstants;
import com.player.chat.entity.ChatHistorySummaryEntity;
import com.player.chat.mapper.ChatMapper;
import com.player.common.entity.ChatModelEntity;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.http.client.spring.restclient.SpringRestClient;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 会话记录压缩（概要总结）服务。
 * <p>
 * 规则：MongoDB 的会话记忆里，原始会话消息（不含 system）超过 {@link SystemtConstants#CHAT_HISTORY_SUMMARY_THRESHOLD} 条时，
 * 用当前选中的模型把会话记录压缩成一段概要总结：
 * <ul>
 *   <li>MongoDB：只保留概要（原始会话记录从 MongoDB 删除）；</li>
 *   <li>MySQL：概要同时备份到 {@code chat_history_summary} 表，MongoDB 崩溃后可用它恢复会话记忆；</li>
 *   <li>原始聊天记录仍在 MySQL 的 {@code chat_history} 表里，不做任何修改（用户查历史以数据库为准）。</li>
 * </ul>
 * 下次对话时只把概要发给大模型（不再发原始会话记录）。
 * 与 FastAPI 项目 chat/services/chat_service.py 的会话压缩逻辑保持一致。
 */
@Slf4j
@Service
public class ChatSummaryService {

    @Autowired
    private ChatMapper chatMapper;

    @Autowired
    private MongoChatMemoryStore mongoChatMemoryStore;

    /**
     * 读取会话概要：MongoDB 优先；Mongo 里没有（崩溃 / 过期）时用 MySQL 备份恢复。
     */
    public String getSummary(String userId, String chatId) {
        try {
            String summary = mongoChatMemoryStore.getSummary(chatId);
            if (summary != null && !summary.trim().isEmpty()) {
                return summary;
            }
        } catch (Exception e) {
            log.error("从 MongoDB 读取会话概要失败: chatId={}", chatId, e);
        }

        try {
            String backup = chatMapper.getChatHistorySummary(userId, chatId);
            if (backup != null && !backup.trim().isEmpty()) {
                log.info("MongoDB 中无会话概要，使用 MySQL 备份并恢复回 MongoDB: userId={}, chatId={}", userId, chatId);
                mongoChatMemoryStore.saveSummary(chatId, backup, 0);
                return backup;
            }
        } catch (Exception e) {
            log.error("从 MySQL 读取会话概要失败: userId={}, chatId={}", userId, chatId, e);
        }
        return null;
    }

    /**
     * 会话记录超过阈值时压缩成概要（在会话完成之后调用，建议放后台线程执行）。
     * 已有概要时会一起送进模型，输出合并后的概要（避免丢历史信息）。
     */
    public void summarizeIfNeeded(String userId, String chatId, String modelId, String companyId) {
        try {
            List<ChatMessage> messages = mongoChatMemoryStore.getMessages(chatId);
            List<String> records = new ArrayList<>();
            for (ChatMessage message : messages) {
                if (message instanceof AiMessage) {
                    records.add("助手: " + text(((AiMessage) message).text()));
                } else if (message instanceof UserMessage) {
                    records.add("用户: " + text(((UserMessage) message).singleText()));
                }
                // SystemMessage 不计入会话记录
            }

            if (records.size() <= SystemtConstants.CHAT_HISTORY_SUMMARY_THRESHOLD) {
                return;
            }

            ChatModelEntity chatModelEntity = chatMapper.getModelById(companyId, modelId);
            if (chatModelEntity == null || chatModelEntity.getDisabled() == 1) {
                log.error("会话压缩失败：模型不存在或已禁用, modelId={}", modelId);
                return;
            }
            ChatModel chatModel = getChatModel(chatModelEntity);

            String oldSummary = mongoChatMemoryStore.getSummary(chatId);
            StringBuilder userContent = new StringBuilder();
            if (oldSummary != null && !oldSummary.trim().isEmpty()) {
                userContent.append("【已有概要】\n").append(oldSummary).append("\n\n");
            }
            userContent.append("【新增会话记录】\n").append(String.join("\n", records));

            log.info("开始压缩会话记录: chatId={}, 消息数={}", chatId, records.size());
            String summary = chatModel.chat(List.of(
                            SystemMessage.from(SystemtConstants.CHAT_SUMMARY_PROMPT),
                            UserMessage.from(userContent.toString())))
                    .aiMessage().text();
            if (summary == null || summary.trim().isEmpty()) {
                log.warn("会话压缩结果为空，放弃保存: chatId={}", chatId);
                return;
            }

            // MongoDB：只保留概要（原始会话记录删除）
            mongoChatMemoryStore.saveSummary(chatId, summary, records.size());

            // MySQL：概要备份（原始聊天记录 chat_history 不动）
            ChatHistorySummaryEntity entity = new ChatHistorySummaryEntity();
            entity.setId(summaryId(userId, chatId));
            entity.setChatId(chatId);
            entity.setUserId(userId);
            entity.setSummary(summary);
            entity.setMessageCount(records.size());
            chatMapper.saveChatHistorySummary(entity);

            log.info("会话已压缩: chatId={}, 概要长度={}", chatId, summary.length());
        } catch (Exception e) {
            log.error("会话记录压缩失败: chatId=" + chatId, e);
        }
    }

    /** 会话概要主键：md5(user_id:chat_id)，与 FastAPI 的 hashlib.md5(...).hexdigest() 结果一致（同一会话两端同一行） */
    public static String summaryId(String userId, String chatId) {
        return DigestUtils.md5DigestAsHex((userId + ":" + chatId).getBytes(StandardCharsets.UTF_8));
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }

    /** 按模型配置构建「同步」聊天模型（压缩总结用；与 AssistantSelector 的流式模型同源） */
    private ChatModel getChatModel(ChatModelEntity chatModelEntity) {
        if ("ollama".equals(chatModelEntity.getType())) {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json;charset=utf-8");
            return OllamaChatModel.builder()
                    .baseUrl(chatModelEntity.getBaseUrl())
                    .modelName(chatModelEntity.getModelName())
                    .temperature(0.3)
                    .timeout(Duration.ofMinutes(2))
                    .customHeaders(headers)
                    .httpClientBuilder(SpringRestClient.builder())
                    .build();
        }
        return OpenAiChatModel.builder()
                .baseUrl(chatModelEntity.getBaseUrl())
                .apiKey(chatModelEntity.getApiKey())
                .modelName(chatModelEntity.getModelName())
                .temperature(0.3)
                .timeout(Duration.ofMinutes(2))
                .httpClientBuilder(SpringRestClient.builder())
                .build();
    }
}
