package com.example.translationchat.common.redis.user;

import com.example.translationchat.client.domain.repository.UserRepository;
import com.example.translationchat.common.redis.aop.DistributeLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserLockService {

    private final UserRepository userRepository;

    @DistributeLock(key = "#key")
    public boolean isAvailableEmail(final String key, String email) {
        return !userRepository.existsByEmail(email);

    }

    @DistributeLock(key = "#key")
    public boolean isAvailableName(final String key, String name) {
        return !userRepository.existsByName(name);
    }
}
