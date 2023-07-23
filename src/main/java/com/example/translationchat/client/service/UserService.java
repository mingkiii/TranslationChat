package com.example.translationchat.client.service;

import static com.example.translationchat.common.exception.ErrorCode.ALREADY_EXIST_NAME;
import static com.example.translationchat.common.exception.ErrorCode.ALREADY_REGISTER_USER;

import com.example.translationchat.client.domain.form.SignUpForm;
import com.example.translationchat.client.domain.model.Language;
import com.example.translationchat.client.domain.model.Nationality;
import com.example.translationchat.client.domain.model.User;
import com.example.translationchat.client.domain.repository.UserRepository;
import com.example.translationchat.common.exception.CustomException;
import com.example.translationchat.common.redis.RedisLockUtil;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisLockUtil redisLockUtil;

    // 회원 가입
    @Transactional
    public String signUp(SignUpForm form) {
        String email = form.getEmail();
        String name = form.getName();
        // 이메일 중복 체크
        if (existsByEmail(email)) {
            throw new CustomException(ALREADY_REGISTER_USER);
        }
        // 이름 중복 체크
        if (existsByName(name)) {
            throw new CustomException(ALREADY_EXIST_NAME);
        }

        userRepository.save(
            User.builder()
                .email(email)
                .password(passwordEncoder.encode(form.getPassword()))
                .name(name)
                .nationality(Nationality.valueOf(form.getNationality()))
                .language(Language.valueOf(form.getLanguage()))
                .randomApproval(true)
                .build()
        );

        return "회원가입이 완료되었습니다.";
    }
    private boolean existsByEmail(String email) {
        String lockKey = "email-lock-" + email;
        return checkExistenceWithLock(lockKey, () -> userRepository.existsByEmail(email));
    }

    private boolean existsByName(String name) {
        String lockKey = "name-lock-" + name;
        return checkExistenceWithLock(lockKey, () -> userRepository.existsByName(name));
    }

    private boolean checkExistenceWithLock(String lockKey, Supplier<Boolean> existenceChecker) {
        try {
            // 락 확보 시도 (타임아웃은 예시로 10초로 설정)
            boolean locked = redisLockUtil.getLock(lockKey, 10);
            if (locked) {
                // 락 확보 성공하면 중복 체크 수행
                return existenceChecker.get();
            } else {
                // 락 확보 실패 시에는 다른 클라이언트가 이미 해당 키로 락을 확보한 것으로 간주
                return true; // 중복된 이메일 또는 이름으로 간주
            }
        } finally {
            // 락 해제
            redisLockUtil.unLock(lockKey);
        }
    }
}
