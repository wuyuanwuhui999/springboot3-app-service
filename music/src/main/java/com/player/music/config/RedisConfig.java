package com.player.music.config;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    // 通用 RedisTemplate
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer()); // 使用默认序列化器
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    // 专门用于 Message 类型的 RedisTemplate
    @Bean
    public RedisTemplate<String, Message> messageRedisTemplate(
            RedisConnectionFactory factory
    ) {
        RedisTemplate<String, Message> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());

        // 使用标准序列化器（不添加类型信息）
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }

    // 移除 redisObjectMapper() 方法
}