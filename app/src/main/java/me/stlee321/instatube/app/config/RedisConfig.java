package me.stlee321.instatube.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@PropertySource("classpath:application-redis.properties")
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    String redisHost;
    @Value("${spring.data.redis.port}")
    Integer redisPort;
    @Bean
    RedisConnectionFactory connectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }
    @Bean
    @Primary
    RedisTemplate<String, String> redisTemplate() {
        var template = new RedisTemplate<String, String>();
        template.setConnectionFactory(connectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    public static String getLogoutPrefix() {
        return "instatube:logout:";
    }
}
