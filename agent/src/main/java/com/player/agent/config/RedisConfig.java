package com.player.agent.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPooled;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.username:#{null}}")
    private String redisUsername;

    @Value("${spring.data.redis.password:#{null}}")
    private String redisPassword;

    @Lazy
    @Bean
    public JedisPooled jedisPooled() {
        if (redisUsername != null && !redisUsername.isEmpty()
                && redisPassword != null && !redisPassword.isEmpty()) {
            return new JedisPooled(redisHost, redisPort, redisUsername, redisPassword);
        } else if (redisPassword != null && !redisPassword.isEmpty()) {
            return new JedisPooled(redisHost, redisPort, "default", redisPassword);
        } else {
            return new JedisPooled(redisHost, redisPort);
        }
    }

    @Lazy
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 配置键序列化器
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // 配置值序列化器 - 使用自定义的 ObjectMapper
        GenericJackson2JsonRedisSerializer serializer = createMessageSerializer();

        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    // 专门用于 Message 类型的 RedisTemplate - 修复版
    @Lazy
    @Bean
    public RedisTemplate<String, Message> messageRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Message> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 配置键序列化器
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // 配置值序列化器 - 使用专门为 Message 配置的序列化器
        GenericJackson2JsonRedisSerializer serializer = createMessageSerializer();

        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 创建专门用于 Spring AI Message 的序列化器 - 修复版
     */
    private GenericJackson2JsonRedisSerializer createMessageSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 注册 JavaTime 模块
        objectMapper.registerModule(new JavaTimeModule());

        // 配置多态类型处理
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(Message.class)
                .allowIfSubType("org.springframework.ai.chat.messages")
                .allowIfSubType("java.util")
                .allowIfSubType("java.time")
                .build();

        // 激活默认类型信息
        objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);

        // 添加 Mixin 来解决反序列化问题
        objectMapper.addMixIn(Message.class, MessageMixin.class);
        objectMapper.addMixIn(UserMessage.class, UserMessageMixin.class);
        objectMapper.addMixIn(SystemMessage.class, SystemMessageMixin.class);
        objectMapper.addMixIn(AssistantMessage.class, AssistantMessageMixin.class);

        // 配置忽略未知属性
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    // 修复后的 Mixin 类 - 添加必要的构造函数和属性

    @JsonIgnoreProperties(ignoreUnknown = true)
    abstract static class MessageMixin {
        @JsonCreator
        public MessageMixin(
                @JsonProperty("content") String content,
                @JsonProperty("metadata") java.util.Map<String, Object> metadata) {
        }

        @JsonProperty("messageType")
        abstract String getMessageType();

        @JsonProperty("content")
        abstract String getContent();

        @JsonProperty("metadata")
        abstract java.util.Map<String, Object> getMetadata();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    abstract static class UserMessageMixin {
        @JsonCreator
        public UserMessageMixin(
                @JsonProperty("content") String content,
                @JsonProperty("metadata") java.util.Map<String, Object> metadata) {
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    abstract static class SystemMessageMixin {
        @JsonCreator
        public SystemMessageMixin(
                @JsonProperty("content") String content,
                @JsonProperty("metadata") java.util.Map<String, Object> metadata) {
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    abstract static class AssistantMessageMixin {
        @JsonCreator
        public AssistantMessageMixin(
                @JsonProperty("content") String content,
                @JsonProperty("metadata") java.util.Map<String, Object> metadata) {
        }
    }
}