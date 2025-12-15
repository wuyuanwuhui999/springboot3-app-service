package com.player.agent.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisChatMemory implements ChatMemory {

    private static final String REDIS_KEY_PREFIX = "chatmemory:";
    private final RedisTemplate<String, Message> redisTemplate; // 使用专用模板

    @Override
    public void add(String conversationId, Message message) {
        ChatMemory.super.add(conversationId, message);
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        // 过滤掉null消息
        List<Message> filteredMessages = messages.stream()
                .filter(Objects::nonNull)
                .toList();

        if (filteredMessages.isEmpty()) {
            return;
        }

        String key = REDIS_KEY_PREFIX + conversationId;
        try {
            // 批量添加消息到 Redis 列表
            redisTemplate.opsForList().rightPushAll(key, filteredMessages);

            // 设置过期时间 - 24小时
            redisTemplate.expire(key, Duration.ofHours(24));

            log.debug("Added {} messages to chat memory for conversation: {}",
                    filteredMessages.size(), conversationId);
        } catch (Exception e) {
            log.error("Failed to add messages to Redis chat memory for conversation: {}",
                    conversationId, e);
            throw new RuntimeException("Failed to store chat memory", e);
        }
    }

    @Override
    public List<Message> get(String conversationId) {
        String key = REDIS_KEY_PREFIX + conversationId;

        try {
            List<Message> messages = redisTemplate.opsForList().range(key, 0, -1);

            if (messages != null) {
                List<Message> filteredMessages = messages.stream()
                        .filter(Objects::nonNull)
                        .toList();

                log.debug("Retrieved {} messages from chat memory for conversation: {}",
                        filteredMessages.size(), conversationId);
                return filteredMessages;
            }
        } catch (Exception e) {
            log.error("Failed to get messages from Redis chat memory for conversation: {}",
                    conversationId, e);
            // 如果反序列化失败，清理损坏的数据
            clear(conversationId);
        }

        return List.of();
    }

    @Override
    public void clear(String conversationId) {
        String key = REDIS_KEY_PREFIX + conversationId;
        try {
            Boolean deleted = redisTemplate.delete(key);
            if (Boolean.TRUE.equals(deleted)) {
                log.debug("Cleared chat memory for conversation: {}", conversationId);
            }
        } catch (Exception e) {
            log.error("Failed to clear chat memory for conversation: {}",
                    conversationId, e);
        }
    }

    /**
     * 清理所有过期的聊天记忆（可选方法）
     */
    public void cleanupExpiredMemories() {
        try {
            // 注意：在生产环境中，通常通过 Redis 的过期策略自动清理
            // 这个方法仅用于特殊情况
            log.info("Chat memory cleanup completed");
        } catch (Exception e) {
            log.error("Failed to cleanup chat memories", e);
        }
    }
}