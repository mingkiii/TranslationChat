package com.example.translationchat.client.service;

import static com.example.translationchat.common.exception.ErrorCode.ALREADY_REGISTERED_EMAIL;
import static com.example.translationchat.common.exception.ErrorCode.ALREADY_REGISTERED_NAME;
import static com.example.translationchat.common.exception.ErrorCode.LOCK_FAILED;
import static com.example.translationchat.common.exception.ErrorCode.LOGIN_FAIL;
import static com.example.translationchat.common.exception.ErrorCode.NOT_FOUND_USER;

import com.example.translationchat.client.domain.dto.MyInfoDto;
import com.example.translationchat.client.domain.dto.UserInfoDto;
import com.example.translationchat.client.domain.form.LoginForm;
import com.example.translationchat.client.domain.form.SignUpForm;
import com.example.translationchat.client.domain.form.UpdateUserForm;
import com.example.translationchat.client.domain.model.User;
import com.example.translationchat.client.domain.repository.UserRepository;
import com.example.translationchat.client.domain.type.ActiveStatus;
import com.example.translationchat.client.domain.type.Language;
import com.example.translationchat.client.domain.type.Nationality;
import com.example.translationchat.common.exception.CustomException;
import com.example.translationchat.common.redis.util.RedisLockUtil;
import com.example.translationchat.common.security.JwtAuthenticationProvider;
import com.example.translationchat.common.security.principal.PrincipalDetails;
import java.util.List;
import java.util.stream.Collectors;
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

        try {
            // 락 확보 시도 (타임아웃은 예시로 5초로 설정)
            boolean emailLocked = redisLockUtil.getLock(email, 5);
            boolean nameLocked = redisLockUtil.getLock(name, 5);
            if (emailLocked && nameLocked) {
                // 락 확보 성공하면 중복 체크 수행
                if (userRepository.existsByEmail(email)) {
                    throw new CustomException(ALREADY_REGISTERED_EMAIL);
                }
                if (userRepository.existsByName(name)) {
                    throw new CustomException(ALREADY_REGISTERED_NAME);
                }
                userRepository.save(
                    User.builder()
                        .email(email)
                        .password(passwordEncoder.encode(form.getPassword()))
                        .name(name)
                        .nationality(form.getNationality())
                        .language(form.getLanguage())
                        .randomApproval(true)
                        .build()
                );
                return "회원가입이 완료되었습니다.";
            } else {
                // 락 확보 실패 시에는 다른 클라이언트가 이미 해당 키로 락을 확보한 것으로 간주
                throw new CustomException(LOCK_FAILED); // 중복된 이메일 또는 이름으로 간주
            }
        } catch (Exception e) {
            redisLockUtil.unLock(email);
            redisLockUtil.unLock(name);
            throw e;
        } finally {
            redisLockUtil.unLock(email);
            redisLockUtil.unLock(name);
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
        if (authentication != null && authentication.isAuthenticated()) {
            PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();

            User user = principalDetails.getUser();
            // 로그인 시 활성 상태가 된다.
            user.setStatus(ActiveStatus.ONLINE);
            userRepository.save(user);

            return provider.createToken(user);
        } else {
            throw new CustomException(LOGIN_FAIL);
        }
    }

    // 로그아웃
    public void logout(Authentication authentication) {
        User user = getUser(authentication);
        // 로그아웃 시 활성 상태를 오프라인으로 업데이트
        user.setStatus(ActiveStatus.OFFLINE);
        userRepository.save(user);
    }

    // 회원 탈퇴
    @Transactional
    public void delete(Authentication authentication) {
        User user = getUser(authentication);
        userRepository.delete(user);
    }

    // 회원(본인) 정보 조회
    public MyInfoDto getInfo(Authentication authentication) {
        User user = getUser(authentication);
        return MyInfoDto.from(user);
    }

    // 회원(본인) 정보 수정
    @Transactional
    public MyInfoDto updateInfo(Authentication authentication, UpdateUserForm form) {
        User user = getUser(authentication);

        // 이름 변경
        String newName = form.getName();
        if (!newName.isEmpty() && !newName.equals(user.getName())) {
            // 이름 중복 체크
            try {
                boolean nameLock = redisLockUtil.getLock(newName, 5);
                if (nameLock) {
                    if (userRepository.existsByName(newName)) {
                        throw new CustomException(ALREADY_REGISTERED_NAME);
                    }
                    user.setName(newName);
                }
            } catch (Exception e) {
                redisLockUtil.unLock(newName);
                throw e;
            } finally {
                redisLockUtil.unLock(newName);
            }
        }

        // 비밀번호 변경
        String newPassword = form.getPassword();
        if (!newPassword.isEmpty() && !passwordEncoder.matches(newPassword,
            user.getPassword())) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        // 국적 변경
        Nationality newNationality = form.getNationality();
        if (!newNationality.equals(user.getNationality())) {
            user.setNationality(newNationality);
        }

        // 언어 변경
        Language newLanguage = form.getLanguage();
        if (!newLanguage.equals(user.getLanguage())) {
            user.setLanguage(newLanguage);
        }

        // 활성 상태 변경
        user.setStatus(form.getStatus());

        userRepository.save(user);

        return MyInfoDto.from(user); // 변경된 유저 정보를 반환
    }

    // 다른 유저 검색
    public List<UserInfoDto> searchByUserName(String name) {
        List<User> users = userRepository.searchByName(name);
        if (users.isEmpty()) {
            throw new CustomException(NOT_FOUND_USER);
        }
        return users.stream()
            .map(UserInfoDto::from)
            .collect(Collectors.toList());
    }

    private User getUser(Authentication authentication) {
        PrincipalDetails details = (PrincipalDetails) authentication.getPrincipal();
        return details.getUser();
    }
}
