package me.stlee321.instatube.app.config;

import me.stlee321.instatube.jwt.SubjectValidator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class LogoutSubjectValidator implements SubjectValidator {
    RedisTemplate<String, String> redisTemplate;

    public LogoutSubjectValidator(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    @Override
    public boolean isValidSubject(String subject) {
        String key = RedisConfig.getLogoutPrefix() + subject;
        String value = redisTemplate.opsForValue().get(key);
        return value == null;
    }
}
