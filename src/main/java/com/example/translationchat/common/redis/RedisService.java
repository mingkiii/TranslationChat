package com.example.translationchat.common.redis;

import com.example.translationchat.domain.user.entity.User;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, User> userRedisTemplate;

    public boolean getLock(String key, long timeoutInSeconds) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        Boolean locked = ops.setIfAbsent(key, "locked", timeoutInSeconds, TimeUnit.SECONDS);
        return locked != null && locked;
    }

    public void unLock(String key) {
        redisTemplate.delete(key);
    }

    public void push(String key, User user) {
        userRedisTemplate.opsForList().leftPush(key, user);
    }

    public User pop(String key) {
        return userRedisTemplate.opsForList().rightPop(key);
    }
}