package com.player.agent.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.IndexOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 会话记忆存储（Spring AI {@link ChatMemory}）—— 从 Redis 迁移到 MongoDB。
 * <p>
 * 会话上下文（对话记忆）存入 MongoDB：数据库 {@code chat} / 集合 {@code chat_memory}，
 * 文档 {@code _id} = conversationId，{@code messages} = 消息 JSON 数组，
 * {@code update_time} 字段上建 TTL 索引实现 180 天过期。
 * <p>
 * 完整聊天记录仍由 {@code AgentWebSocketHandler} 写入 MySQL 的 chat_history 表（保留双写）。
 */
@Slf4j
@Component
public class MongoChatMemory implements ChatMemory {

    private static final String COLLECTION = "chat_memory";
    private static final long TTL_DAYS = 180L;
    /** 会话上下文最多保留的消息条数（与 FastAPI agent 模块保持一致） */
    private static final int MAX_MESSAGES = 20;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 会话记忆的会话ID（= MongoDB 文档 _id）。
     * 与 FastAPI agent 模块的 {@code agent_history:{userId}:{chatId}} 保持一致，
     * 避免所有会话都落到默认的 {@code default} 文档里互相覆盖。
     */
    public static String conversationId(String userId, String chatId) {
        return "agent_history:" + userId + ":" + chatId;
    }

    @PostConstruct
    public void initTtlIndex() {
        try {
            mongoTemplate.getCollection(COLLECTION).createIndex(
                    new Document("update_time", 1),
                    new IndexOptions().expireAfter(TTL_DAYS, TimeUnit.DAYS));
        } catch (Exception e) {
            // MongoDB 暂不可用或索引已存在，忽略
            log.warn("创建 MongoDB 会话记忆 TTL 索引失败: {}", e.getMessage());
        }
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        List<Message> filtered = messages.stream().filter(Objects::nonNull).toList();
        if (filtered.isEmpty()) {
            return;
        }
        try {
            List<Message> all = new ArrayList<>(get(conversationId));
            all.addAll(filtered);
            if (all.size() > MAX_MESSAGES) {
                all = new ArrayList<>(all.subList(all.size() - MAX_MESSAGES, all.size()));
            }
            Query query = new Query(Criteria.where("_id").is(conversationId));
            Update update = new Update()
                    .set("messages", toJson(all))
                    .set("update_time", new Date());
            mongoTemplate.upsert(query, update, COLLECTION);
            log.debug("会话记忆已保存 - 会话ID: {}, 消息数: {}", conversationId, all.size());
        } catch (Exception e) {
            // 不抛出异常，避免影响主要业务逻辑
            log.error("保存会话记忆到 MongoDB 失败 - 会话ID: {}", conversationId, e);
        }
    }

    @Override
    public List<Message> get(String conversationId) {
        try {
            Document doc = mongoTemplate.findOne(
                    new Query(Criteria.where("_id").is(conversationId)),
                    Document.class, COLLECTION);
            if (doc == null) {
                return new ArrayList<>();
            }
            String json = doc.getString("messages");
            return json == null ? new ArrayList<>() : fromJson(json);
        } catch (Exception e) {
            log.error("从 MongoDB 获取会话记忆失败 - 会话ID: {}", conversationId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public void clear(String conversationId) {
        try {
            mongoTemplate.remove(new Query(Criteria.where("_id").is(conversationId)), COLLECTION);
            log.debug("会话记忆已清理 - 会话ID: {}", conversationId);
        } catch (Exception e) {
            log.error("清理会话记忆失败 - 会话ID: {}", conversationId, e);
        }
    }

    /** 序列化：只持久化消息类型与文本内容（会话记忆不需要 tool 调用细节） */
    private String toJson(List<Message> messages) {
        List<StoredMessage> stored = new ArrayList<>();
        for (Message message : messages) {
            stored.add(new StoredMessage(message.getMessageType().name(), message.getText()));
        }
        try {
            return objectMapper.writeValueAsString(stored);
        } catch (Exception e) {
            throw new IllegalStateException("会话消息序列化失败", e);
        }
    }

    /** 反序列化：按 messageType 还原 Spring AI 消息 */
    private List<Message> fromJson(String json) {
        List<Message> messages = new ArrayList<>();
        try {
            StoredMessage[] stored = objectMapper.readValue(json, StoredMessage[].class);
            for (StoredMessage item : stored) {
                MessageType type = MessageType.valueOf(item.messageType());
                switch (type) {
                    case USER -> messages.add(new UserMessage(item.content()));
                    case ASSISTANT -> messages.add(new AssistantMessage(item.content()));
                    case SYSTEM -> messages.add(new SystemMessage(item.content()));
                    default -> log.debug("跳过不支持持久化的消息类型: {}", type);
                }
            }
        } catch (Exception e) {
            log.warn("解析 MongoDB 会话记忆失败: {}", e.getMessage());
        }
        return messages;
    }

    /** MongoDB 中存储的消息结构 */
    private record StoredMessage(String messageType, String content) {
    }
}
