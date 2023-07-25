package com.example.translationchat.client.service;

import static com.example.translationchat.common.exception.ErrorCode.ALREADY_REGISTERED_EMAIL;
import static com.example.translationchat.common.exception.ErrorCode.ALREADY_REGISTERED_NAME;
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
import com.example.translationchat.common.redis.user.UserLockService;
import com.example.translationchat.common.security.JwtAuthenticationProvider;
import com.example.translationchat.common.security.principal.PrincipalDetails;
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

    private static final String EMAIL_KEY_PREFIX = "EMAIL_";
    private static final String NAME_KEY_PREFIX = "NAME_";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserLockService userLockService;
    private final JwtAuthenticationProvider provider;
    private final AuthenticationManager authenticationManager;

    // 회원 가입
    @Transactional
    public String signUp(SignUpForm form) {
        String email = form.getEmail();
        String name = form.getName();
        // 이메일 중복 체크
        if (!userLockService.isAvailableEmail(EMAIL_KEY_PREFIX + email, email)) {
            throw new CustomException(ALREADY_REGISTERED_EMAIL);
        }
        // 이름 중복 체크
        if (!userLockService.isAvailableName(NAME_KEY_PREFIX + name, name)) {
            throw new CustomException(ALREADY_REGISTERED_NAME);
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
        if (authentication != null && authentication.isAuthenticated()) {
            PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();

            Long authenticatedId = principalDetails.getUser().getId();
            String authenticatedEmail = principalDetails.getUser().getEmail();

            return provider.createToken(authenticatedId, authenticatedEmail);
        } else {
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
        } else {
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
        } else {
            // 로그인 후 토큰이 만료된 경우
            throw new CustomException(LOGIN_REQUIRED);
        }
    }

    // 회원(본인) 정보 수정
    @Transactional
    public UserInfoDto updateInfo(Authentication authentication,
        UpdateUserForm form) {
        if (authentication != null && authentication.isAuthenticated()) {
            User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

            // 이름 변경
            String newName = form.getName();
            if (!newName.isEmpty() && !newName.equals(user.getName())) {
                // 이름 중복 체크
                if (!userLockService.isAvailableName(NAME_KEY_PREFIX + newName, newName)) {
                    throw new CustomException(ALREADY_REGISTERED_NAME);
                }
                user.setName(newName);
            }
            // 비밀번호 변경
            String newPassword = form.getPassword();
            if (!newPassword.isEmpty() && !passwordEncoder.matches(newPassword,
                user.getPassword())) {
                user.setPassword(passwordEncoder.encode(newPassword));
            }
            // 국적 변경
            String newNationality = form.getNationality();
            if (!newNationality.isEmpty() && !newNationality.equals(
                String.valueOf(user.getNationality()))) {
                user.setNationality(Nationality.toEnumType(newNationality));
            }
            // 언어 변경
            Language newLanguage = Language.toEnumType(form.getLanguage());
            if (!newLanguage.equals(user.getLanguage())) {
                user.setLanguage(newLanguage);
            }

            userRepository.save(user);

            return UserInfoDto.from(user); // 변경된 유저 정보를 반환
        } else {
            // 로그인 후 토큰이 만료된 경우
            throw new CustomException(LOGIN_REQUIRED);
        }
    }
}
