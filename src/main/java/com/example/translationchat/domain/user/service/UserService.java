package com.example.translationchat.domain.user.service;

import static com.example.translationchat.common.exception.ErrorCode.ALREADY_REGISTERED_EMAIL;
import static com.example.translationchat.common.exception.ErrorCode.ALREADY_REGISTERED_NAME;
import static com.example.translationchat.common.exception.ErrorCode.LOGIN_FAIL;
import static com.example.translationchat.common.exception.ErrorCode.NEW_PASSWORD_MISMATCH_RE_PASSWORD;
import static com.example.translationchat.common.exception.ErrorCode.NOT_FOUND_USER;
import static com.example.translationchat.common.exception.ErrorCode.USER_PASSWORD_EQUALS_NEW_PASSWORD;
import static com.example.translationchat.common.exception.ErrorCode.USER_PASSWORD_MISMATCH;

import com.example.translationchat.common.exception.CustomException;
import com.example.translationchat.common.security.JwtAuthenticationProvider;
import com.example.translationchat.common.security.principal.PrincipalDetails;
import com.example.translationchat.domain.notification.form.NotificationForm;
import com.example.translationchat.domain.type.ActiveStatus;
import com.example.translationchat.domain.type.ContentType;
import com.example.translationchat.domain.type.Language;
import com.example.translationchat.domain.type.Nationality;
import com.example.translationchat.domain.user.entity.User;
import com.example.translationchat.domain.user.form.LoginForm;
import com.example.translationchat.domain.user.form.SignUpForm;
import com.example.translationchat.domain.user.form.UpdatePasswordForm;
import com.example.translationchat.domain.user.form.UpdateUserForm;
import com.example.translationchat.domain.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthenticationProvider provider;
    private final AuthenticationManager authenticationManager;

    public User findById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER));
    }
    // 회원 가입
    @Transactional
    public String signUp(SignUpForm form) {
        String email = form.getEmail();
        if (userRepository.existsByEmail(email)) {
            throw new CustomException(ALREADY_REGISTERED_EMAIL);
        }
        String name = form.getName();
        if (userRepository.existsByName(name)) {
            throw new CustomException(ALREADY_REGISTERED_NAME);
        }

        userRepository.save(User.builder()
                .email(email)
                .password(passwordEncoder.encode(form.getPassword()))
                .name(name)
                .nationality(form.getNationality())
                .language(form.getLanguage())
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

        try {
            Authentication authentication =
                authenticationManager.authenticate(authenticationToken);

            // 인증이 완료된 객체면
            if (authentication != null && authentication.isAuthenticated()) {
                PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
                User user = getUserByEmail(principalDetails.getEmail());
                // 로그인 시 활성 상태가 된다.
                user.setStatus(ActiveStatus.ONLINE);
                userRepository.save(user);
                return provider.createToken(user);
            } else {
                throw new CustomException(LOGIN_FAIL);
            }
        } catch (AuthenticationException e) {
            throw new CustomException(LOGIN_FAIL);
        }
    }

    // 로그아웃
    public void logout(User user) {
        // 로그아웃 시 활성 상태를 오프라인으로 업데이트
        user.setStatus(ActiveStatus.OFFLINE);
        userRepository.save(user);
    }

    // 회원 탈퇴
    public void delete(User user) {
        userRepository.delete(user);
    }

    // 회원(본인) 정보 수정
    public User updateInfo(User user, UpdateUserForm form) {
        // 국적 변경
        Nationality newNationality = form.getNationality();
        if (newNationality != null && newNationality != user.getNationality()) {
            user.setNationality(newNationality);
        }

        // 언어 변경
        Language newLanguage = form.getLanguage();
        if (newLanguage != null && newLanguage != user.getLanguage()) {
            user.setLanguage(newLanguage);
        }

        return userRepository.save(user);
    }

    // 다른 유저 검색
    public List<User> searchByUserName(String name) {
        return userRepository.searchByName(name);
    }

    // 비밀번호 변경
    public void updatePassword(User user, UpdatePasswordForm form) {
        String userPassword = user.getPassword();
        String password = form.getPassword();
        String newPassword = form.getNewPassword();

        // 실제 비밀번호와 입력한 유저 비밀번호 다르면 예외 발생
        if (!passwordEncoder.matches(password, userPassword)) {
            throw new CustomException(USER_PASSWORD_MISMATCH);
        }
        // 새 비밀번호와 새 비밀번호 확인 이 다르면 예외 발생
        if (!newPassword.equals(form.getRePassword())) {
            throw new CustomException(NEW_PASSWORD_MISMATCH_RE_PASSWORD);
        }
        // 기존 비밀번호랑 새 비밀번호 같으면 예외 발생, 다르면 변경
        if (password.equals(newPassword)) {
            throw new CustomException(USER_PASSWORD_EQUALS_NEW_PASSWORD);
        } else {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        }
    }

    @Transactional
    public void updateWarningCount(User user) {
        user.setWarningCount(user.getWarningCount() + 1);
        if (user.getWarningCount() % 3 == 0) {
            user.setRandomApproval(false); // 이용 정지 상태로 변경
            eventPublisher.publishEvent(
                NotificationForm.of(user, null, ContentType.INVALID_RANDOM_CHAT));
        }
        userRepository.save(user);
    }

    public void updateRandomApproval(User user) {
        user.setRandomApproval(true);
        userRepository.save(user);
    }
}
