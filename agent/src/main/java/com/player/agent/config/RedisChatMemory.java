package com.player.agent.config;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
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

        String key = REDIS_KEY_PREFIX + conversationId;
        redisTemplate.opsForList().rightPushAll(key, filteredMessages);
    }


    @Override
    public List<Message> get(String conversationId) {
        String key = REDIS_KEY_PREFIX + conversationId;
        List<Message> serializedMessages = redisTemplate.opsForList().range(key, 0, -1);

        if (serializedMessages != null) {
            return serializedMessages.stream()
                    .filter(Objects::nonNull)
                    .toList();
        }
        return List.of();
    }

    @Override
    public void clear(String conversationId) {
        redisTemplate.delete(REDIS_KEY_PREFIX + conversationId);
    }

}
