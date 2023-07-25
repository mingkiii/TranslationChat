package com.example.translationchat.common.redis.user;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.example.translationchat.client.domain.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserLockServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserLockService userLockService;

    @Test
    public void testIsAvailableEmail()
        throws InterruptedException, ExecutionException {
        // given
        String email = "test@example.com";
        String key = "test_key";

        when(userRepository.existsByEmail(email)).thenReturn(true);

        // when
        int numThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        List<Future<Boolean>> results = new ArrayList<>();

        for (int i = 0; i < numThreads; i++) {
            // isAvailableEmail 메서드를 비동기적으로 호출하여 동시에 여러 스레드에서 테스트
            Future<Boolean> future = executorService.submit(() -> userLockService.isAvailableEmail(key, email));
            results.add(future);
        }

        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        // then
        for (Future<Boolean> future : results) {
            boolean result = future.get();
            assertFalse(result); // 이미 존재하므로 모두 false가 반환되어야 함
        }
    }

    @Test
    public void testIsAvailableName()
        throws InterruptedException, ExecutionException {
        // given
        String name = "test_name";
        String key = "test_key";

        when(userRepository.existsByName(name)).thenReturn(false);

        // when
        int numThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        List<Future<Boolean>> results = new ArrayList<>();

        for (int i = 0; i < numThreads; i++) {
            // isAvailableName 메서드를 비동기적으로 호출하여 동시에 여러 스레드에서 테스트
            Future<Boolean> future = executorService.submit(() -> userLockService.isAvailableName(key, name));
            results.add(future);
        }

        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        // then
        for (Future<Boolean> future : results) {
            boolean result = future.get();
            assertTrue(result); // 존재하지 않으므로 모두 true가 반환되어야 함
        }
    }
}