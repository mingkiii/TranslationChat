package com.example.translationchat.common.redis;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisLockUtil {
    private final RedisTemplate<String, String> redisTemplate;

    public boolean getLock(String key, long timeoutInSeconds) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        Boolean locked = ops.setIfAbsent(key, "locked", timeoutInSeconds, TimeUnit.SECONDS);
        return locked != null && locked;
    }

    public void unLock(String key) {
        redisTemplate.delete(key);
    }
}
