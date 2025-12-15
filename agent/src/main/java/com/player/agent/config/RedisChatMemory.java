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
    private final RedisTemplate<String, Message> messageRedisTemplate;

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
            log.debug("开始保存聊天记录到Redis - 会话ID: {}, 消息数量: {}", conversationId, filteredMessages.size());

            // 先清理旧的聊天记录，避免积累过多
            clear(conversationId);

            // 批量添加消息到 Redis 列表
            messageRedisTemplate.opsForList().rightPushAll(key, filteredMessages.toArray(new Message[0]));

            // 设置过期时间 - 24小时
            messageRedisTemplate.expire(key, Duration.ofHours(24));

            log.info("成功保存 {} 条消息到聊天记忆 - 会话ID: {}",
                    filteredMessages.size(), conversationId);
        } catch (Exception e) {
            log.error("保存消息到Redis聊天记忆失败 - 会话ID: {}", conversationId, e);
            // 不抛出异常，避免影响主要业务逻辑
            log.warn("聊天记忆保存失败，但继续处理主要业务");
        }
    }

    @Override
    public List<Message> get(String conversationId) {
        String key = REDIS_KEY_PREFIX + conversationId;

        try {
            log.debug("从Redis获取聊天记忆 - 会话ID: {}", conversationId);

            // 获取所有消息
            List<Message> messages = messageRedisTemplate.opsForList().range(key, 0, -1);

            if (messages != null && !messages.isEmpty()) {
                // 过滤null值
                List<Message> filteredMessages = messages.stream()
                        .filter(Objects::nonNull)
                        .toList();

                log.info("从聊天记忆成功获取 {} 条消息 - 会话ID: {}",
                        filteredMessages.size(), conversationId);
                return filteredMessages;
            } else {
                log.debug("聊天记忆中没有找到消息 - 会话ID: {}", conversationId);
                return List.of();
            }
        } catch (Exception e) {
            log.error("从Redis获取聊天记忆失败 - 会话ID: {}", conversationId, e);
            // 如果反序列化失败，清理损坏的数据
            try {
                clear(conversationId);
                log.info("已清理损坏的聊天记忆数据 - 会话ID: {}", conversationId);
            } catch (Exception clearEx) {
                log.error("清理损坏聊天记忆数据失败 - 会话ID: {}", conversationId, clearEx);
            }

            // 返回空列表而不是抛出异常
            return List.of();
        }
    }

    @Override
    public void clear(String conversationId) {
        String key = REDIS_KEY_PREFIX + conversationId;
        try {
            Boolean deleted = messageRedisTemplate.delete(key);
            if (Boolean.TRUE.equals(deleted)) {
                log.debug("清理聊天记忆成功 - 会话ID: {}", conversationId);
            } else {
                log.debug("聊天记忆不存在或已清理 - 会话ID: {}", conversationId);
            }
        } catch (Exception e) {
            log.error("清理聊天记忆失败 - 会话ID: {}", conversationId, e);
        }
    }

    /**
     * 清理所有过期的聊天记忆（可选方法）
     */
    public void cleanupExpiredMemories() {
        try {
            // 注意：Redis会自动清理过期key
            // 这个方法仅用于特殊清理需求
            log.info("聊天记忆清理完成");
        } catch (Exception e) {
            log.error("聊天记忆清理失败", e);
        }
    }

    /**
     * 获取聊天记忆大小
     */
    public Long getMemorySize(String conversationId) {
        String key = REDIS_KEY_PREFIX + conversationId;
        try {
            return messageRedisTemplate.opsForList().size(key);
        } catch (Exception e) {
            log.error("获取聊天记忆大小失败 - 会话ID: {}", conversationId, e);
            return 0L;
        }
    }
}