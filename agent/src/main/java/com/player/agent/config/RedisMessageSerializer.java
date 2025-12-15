package com.player.agent.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Component;

@Component
public class RedisMessageSerializer implements RedisSerializer<Message> {

    private final ObjectMapper objectMapper;

    public RedisMessageSerializer() {
        this.objectMapper = new ObjectMapper();
        // 配置 objectMapper 来处理 Message 类
        configureObjectMapper();
    }

    private void configureObjectMapper() {
        // 注册 JavaTime 模块
        objectMapper.findAndRegisterModules();

        // 配置多态类型处理
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY
        );
    }

    @Override
    public byte[] serialize(Message message) throws SerializationException {
        if (message == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsBytes(message);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Could not serialize message", e);
        }
    }

    @Override
    public Message deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return objectMapper.readValue(bytes, Message.class);
        } catch (Exception e) {
            throw new SerializationException("Could not deserialize message", e);
        }
    }
}