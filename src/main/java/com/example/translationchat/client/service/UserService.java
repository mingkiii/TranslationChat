package com.example.translationchat.client.service;

import static com.example.translationchat.common.exception.ErrorCode.ALREADY_EXIST_NAME;
import static com.example.translationchat.common.exception.ErrorCode.ALREADY_REGISTER_USER;
import static com.example.translationchat.common.exception.ErrorCode.LOGIN_FAIL;
import static com.example.translationchat.common.exception.ErrorCode.LOGIN_REQUIRED;
import static com.example.translationchat.common.exception.ErrorCode.NOT_FOUND_USER;

import com.example.translationchat.client.domain.dto.UserInfoDto;
import com.example.translationchat.client.domain.form.LoginForm;
import com.example.translationchat.client.domain.form.SignUpForm;
import com.example.translationchat.client.domain.form.UpdateUserForm;
import com.example.translationchat.client.domain.model.Language;
import com.example.translationchat.client.domain.model.Nationality;
import com.example.translationchat.client.domain.model.User;
import com.example.translationchat.client.domain.repository.UserRepository;
import com.example.translationchat.common.exception.CustomException;
import com.example.translationchat.common.redis.RedisLockUtil;
import com.example.translationchat.common.security.JwtAuthenticationProvider;
import com.example.translationchat.common.security.principal.PrincipalDetails;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisLockUtil redisLockUtil;
    private final JwtAuthenticationProvider provider;
    private final AuthenticationManager authenticationManager;

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
                .language(Language.toEnumType(form.getLanguage()))
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

    // 로그인 (반환값 : 토큰)
    // AuthenticationManager 에서 회원 인증 처리
    public String login(LoginForm form) {
        String email = form.getEmail();
        String password = form.getPassword();

        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(email, password);

        Authentication authentication =
            authenticationManager.authenticate(authenticationToken);

        // 인증이 완료된 객체면
        if(authentication != null && authentication.isAuthenticated()) {
            PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();

            Long authenticatedId = principalDetails.getUser().getId();
            String authenticatedEmail = principalDetails.getUser().getEmail();

            return provider.createToken(authenticatedId, authenticatedEmail);
        }else {
            throw new CustomException(LOGIN_FAIL);
        }
    }

    // 회원 탈퇴
    @Transactional
    public void delete(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new CustomException(NOT_FOUND_USER));
            userRepository.delete(user);
        }else {
            // 로그인 후 토큰이 만료된 경우
            throw new CustomException(LOGIN_REQUIRED);
        }
    }

    // 회원(본인) 정보 조회
    public UserInfoDto getInfo(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new CustomException(NOT_FOUND_USER));
            return UserInfoDto.from(user);
        }else {
            // 로그인 후 토큰이 만료된 경우
            throw new CustomException(LOGIN_REQUIRED);
        }
    }

    // 회원(본인) 정보 수정
    @Transactional
    public UserInfoDto updateInfo(Authentication authentication, UpdateUserForm form) {
        if (authentication != null && authentication.isAuthenticated()) {
            User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

            // 이름 변경
            String newName = form.getName();
            if (!newName.isEmpty() && !newName.equals(user.getName())) {
                // 이름 중복 체크
                if (existsByName(newName)) {
                    throw new CustomException(ALREADY_EXIST_NAME);
                }
                user.setName(newName);
            }
            // 비밀번호 변경
            String newPassword = form.getPassword();
            if (!newPassword.isEmpty() && !passwordEncoder.matches(newPassword, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(newPassword));
            }
            // 국적 변경
            String newNationality = form.getNationality();
            if (!newNationality.isEmpty() && !newNationality.equals(String.valueOf(user.getNationality()))) {
                user.setNationality(Nationality.toEnumType(newNationality));
            }
            // 언어 변경
            Language newLanguage = Language.toEnumType(form.getLanguage());
            if (!newLanguage.equals(user.getLanguage())) {
                user.setLanguage(newLanguage);
            }

            userRepository.save(user);

            return UserInfoDto.from(user); // 변경된 유저 정보를 반환
        }else {
            // 로그인 후 토큰이 만료된 경우
            throw new CustomException(LOGIN_REQUIRED);
        }
    }
}
