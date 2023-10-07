package com.example.translationchat.common.redis;

import com.example.translationchat.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, User> userRedisTemplate;

    public void push(String key, User user) {
        userRedisTemplate.opsForList().leftPush(key, user);
    }

    public User pop(String key) {
        return userRedisTemplate.opsForList().rightPop(key);
    }
}