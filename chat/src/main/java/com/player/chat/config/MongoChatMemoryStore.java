package com.player.chat.config;

import com.mongodb.client.model.IndexOptions;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 会话记忆存储（ChatMemoryStore）—— 从 Redis 迁移到 MongoDB。
 * <p>
 * 会话上下文（对话记忆）存入 MongoDB：数据库 {@code chat} / 集合 {@code chat_memory}，
 * 通过 {@code update_time} 字段 + TTL 索引实现 180 天过期。
 * <p>
 * 完整聊天记录仍由 ChatService#chatWithWebSocketHandling 写入 MySQL 的 chat_history 表（保留双写）。
 */
@Component
public class MongoChatMemoryStore implements ChatMemoryStore {

    private static final String COLLECTION = "chat_memory";
    private static final long TTL_DAYS = 180L;

    @Autowired
    private MongoTemplate mongoTemplate;

    @PostConstruct
    public void initTtlIndex() {
        try {
            mongoTemplate.getCollection(COLLECTION).createIndex(
                    new Document("update_time", 1),
                    new IndexOptions().expireAfter(TTL_DAYS, TimeUnit.DAYS));
        } catch (Exception e) {
            // MongoDB 暂不可用或索引已存在，忽略（后续写入时惰性重建）
        }
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        Document doc = mongoTemplate.findOne(
                new Query(Criteria.where("_id").is(memoryId.toString())),
                Document.class, COLLECTION);
        if (doc == null) {
            return new ArrayList<>();
        }
        String json = doc.getString("messages");
        if (json == null) {
            return new ArrayList<>();
        }
        try {
            return ChatMessageDeserializer.messagesFromJson(json);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> list) {
        String json = ChatMessageSerializer.messagesToJson(list);
        Query query = new Query(Criteria.where("_id").is(memoryId.toString()));
        Update update = new Update()
                .set("messages", json)
                .set("update_time", new Date());
        mongoTemplate.upsert(query, update, COLLECTION);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        mongoTemplate.remove(
                new Query(Criteria.where("_id").is(memoryId.toString())),
                COLLECTION);
    }
}
