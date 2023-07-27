package com.example.translationchat.client.service;

import static com.example.translationchat.common.exception.ErrorCode.ALREADY_REGISTERED_EMAIL;
import static com.example.translationchat.common.exception.ErrorCode.ALREADY_REGISTERED_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import com.example.translationchat.client.domain.model.User;
import com.example.translationchat.client.domain.repository.UserRepository;
import com.example.translationchat.client.domain.form.SignUpForm;
import com.example.translationchat.client.domain.type.Language;
import com.example.translationchat.client.domain.type.Nationality;
import com.example.translationchat.common.exception.CustomException;
import com.example.translationchat.common.redis.util.RedisLockUtil;
import com.example.translationchat.common.security.JwtAuthenticationProvider;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Transactional
class UserServiceTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RedisLockUtil redisLockUtil;

    @Autowired
    private JwtAuthenticationProvider provider;

    @Autowired
    private AuthenticationManager authenticationManager;

    private UserService userService;

    @BeforeEach
    public void setUp() {
        userService = new UserService(userRepository, passwordEncoder,
            redisLockUtil, provider, authenticationManager);
    }

    // 회원가입 테스트
    @Test
    @DisplayName("회원가입_성공")
    public void testSignUp_Success() {
        // given
        SignUpForm signUpForm = SignUpForm.builder()
            .email("test@email.com")
            .password("test123!")
            .name("test")
            .nationality(Nationality.UK)
            .language(Language.FR)
            .build();

        // when
        String result = userService.signUp(signUpForm);
        // then
        assertEquals("회원가입이 완료되었습니다.", result);
    }
    @Test
    @DisplayName("회원가입 동시성 테스트")
    public void testOptimisticLockInSignUp()
        throws InterruptedException {
        // given
        String name = "test";
        // 사용자 정보 생성 - 여러 사람이 동시에 동일한 이메일로 시도하는 경우는 현실적으로 드물다고 생각하여 이름을 동일하게 맞춤.
        SignUpForm form1 = SignUpForm.builder()
            .email("test@example.com")
            .name(name)
            .password("test123!")
            .nationality(Nationality.UK)
            .language(Language.FR)
            .build();
        SignUpForm form2 = SignUpForm.builder()
            .email("test2@example.com")
            .name(name)
            .password("test123!")
            .nationality(Nationality.KOREA)
            .language(Language.KO)
            .build();

        // when
        // 회원가입을 두 개의 스레드에서 동시에 시도합니다.
        CountDownLatch latch = new CountDownLatch(2);
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Future<String> future1 = executorService.submit(() -> {
            latch.countDown();
            try {
                latch.await(); // 모든 스레드가 시작될 때까지 대기
                return userService.signUp(form1);
            } catch (Exception e) {
                return e.getMessage();
            }
        });
        Future<String> future2 = executorService.submit(() -> {
            latch.countDown();
            try {
                latch.await(); // 모든 스레드가 시작될 때까지 대기
                return userService.signUp(form2);
            } catch (Exception e) {
                return e.getMessage();
            }
        });
        // 스레드 실행이 완료될 때까지 대기합니다.
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        // then
        // 회원가입은 한 번만 성공해야 합니다.
        List<User> users = userRepository.findAll();
        assertEquals(1, users.size());
        // 두 개의 스레드 중 하나만 성공해야 합니다.
        boolean success1 = isSuccessful(future1);
        boolean success2 = isSuccessful(future2);
        if (success1 && !success2) {
            assertEquals("test@example.com", users.get(0).getEmail());
        } else if (!success1 && success2) {
            assertEquals("test2@example.com", users.get(0).getEmail());
        } else {
            fail("두 개의 스레드 중 하나만 성공해야 합니다.");
        }
    }
    private boolean isSuccessful(Future<String> future) {
        try {
            return future.isDone() && future.get().equals("회원가입이 완료되었습니다.");
        } catch (Exception e) {
            return false;
        }
    }
    @Test
    @DisplayName("회원가입_실패-이메일 중복")
    public void testSignUp_DuplicateEmail() {
        // given
        User existingUser = User.builder()
            .email("existing@email.com")
            .password("password")
            .name("existing")
            .nationality(Nationality.KOREA)
            .language(Language.KO)
            .build();
        userRepository.save(existingUser);
        SignUpForm signUpForm = SignUpForm.builder()
            .email(existingUser.getEmail()) // Use existing email to test duplicate
            .password("test123!")
            .name("test")
            .nationality(Nationality.UK)
            .language(Language.FR)
            .build();
        // when
        CustomException exception = assertThrows(
            CustomException.class, () -> userService.signUp(signUpForm));
        // then
        assertEquals(ALREADY_REGISTERED_EMAIL, exception.getErrorCode());
    }
    @Test
    @DisplayName("회원가입_실패-이름 중복")
    public void testSignUp_DuplicateName() {
        // given
        User existingUser = User.builder()
            .email("existing@email.com")
            .password("password")
            .name("existing") // Use existing name to test duplicate
            .nationality(Nationality.KOREA)
            .language(Language.KO)
            .build();
        userRepository.save(existingUser);
        SignUpForm signUpForm = SignUpForm.builder()
            .email("new@email.com")
            .password("test123!")
            .name(existingUser.getName())
            .nationality(Nationality.UK)
            .language(Language.FR)
            .build();
        // when
        CustomException exception = assertThrows(
            CustomException.class, () -> userService.signUp(signUpForm));
        // then
        assertEquals(ALREADY_REGISTERED_NAME, exception.getErrorCode());
    }
}