package com.example.translationchat.client.service;

import static com.example.translationchat.common.exception.ErrorCode.ALREADY_EXIST_NAME;
import static com.example.translationchat.common.exception.ErrorCode.ALREADY_REGISTER_USER;

import com.example.translationchat.client.domain.form.SignUpForm;
import com.example.translationchat.client.domain.model.Language;
import com.example.translationchat.client.domain.model.Nationality;
import com.example.translationchat.client.domain.model.User;
import com.example.translationchat.client.domain.repository.UserRepository;
import com.example.translationchat.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원 가입
    @Transactional
    public String signUp(SignUpForm form) {
        String email = form.getEmail();
        String name = form.getName();
        // 이메일 중복 체크
        if (userRepository.existsByEmail(email)) {
            throw new CustomException(ALREADY_REGISTER_USER);
        }

        // 이름 중복 체크
        if (userRepository.existsByName(name)) {
            throw new CustomException(ALREADY_EXIST_NAME);
        }

        userRepository.save(
            User.builder()
                .email(email)
                .password(passwordEncoder.encode(form.getPassword()))
                .name(name)
                .nationality(Nationality.valueOf(form.getNationality()))
                .language(Language.valueOf(form.getLanguage()))
                .random_approval(true)
                .build()
        );

        return "회원가입이 완료되었습니다.";
    }
}
