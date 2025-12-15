package com.player.agent.config;

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
     * 创建专门用于 Spring AI Message 的序列化器
     */
    private GenericJackson2JsonRedisSerializer createMessageSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 注册 JavaTime 模块
        objectMapper.registerModule(new JavaTimeModule());

        // 配置多态类型处理 - 这是关键！
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(Message.class)  // 允许 Message 及其子类
                .allowIfSubType("org.springframework.ai.chat.messages")
                .allowIfSubType("java.util")
                .allowIfSubType("java.time")
                .build();

        // 激活默认类型信息，使用属性方式存储类型信息
        objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        // 添加 Mixin 来解决 UserMessage 等类缺少默认构造函数的问题
        objectMapper.addMixIn(Message.class, MessageMixin.class);
        objectMapper.addMixIn(UserMessage.class, UserMessageMixin.class);
        objectMapper.addMixIn(SystemMessage.class, SystemMessageMixin.class);
        objectMapper.addMixIn(AssistantMessage.class, AssistantMessageMixin.class);

        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    // Mixin 类定义 - 解决 Spring AI Message 类的反序列化问题

    abstract static class MessageMixin {
        public MessageMixin() {}  // 添加默认构造函数
        public MessageMixin(String content) {}  // 保留原有构造函数
    }

    abstract static class UserMessageMixin {
        public UserMessageMixin() {}  // 添加默认构造函数
        public UserMessageMixin(String content) {}  // 保留原有构造函数
        public UserMessageMixin(String content, java.util.Map<String, Object> properties) {}  // 保留原有构造函数
    }

    abstract static class SystemMessageMixin {
        public SystemMessageMixin() {}  // 添加默认构造函数
        public SystemMessageMixin(String content) {}  // 保留原有构造函数
        public SystemMessageMixin(String content, java.util.Map<String, Object> properties) {}  // 保留原有构造函数
    }

    abstract static class AssistantMessageMixin {
        public AssistantMessageMixin() {}  // 添加默认构造函数
        public AssistantMessageMixin(String content) {}  // 保留原有构造函数
        public AssistantMessageMixin(String content, java.util.Map<String, Object> properties) {}  // 保留原有构造函数
    }
}