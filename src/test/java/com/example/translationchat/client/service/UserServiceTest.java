package com.example.translationchat.client.service;

import static com.example.translationchat.common.exception.ErrorCode.ALREADY_EXIST_NAME;
import static com.example.translationchat.common.exception.ErrorCode.ALREADY_REGISTER_USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.translationchat.client.domain.form.SignUpForm;
import com.example.translationchat.client.domain.model.Language;
import com.example.translationchat.client.domain.model.Nationality;
import com.example.translationchat.client.domain.model.User;
import com.example.translationchat.client.domain.repository.UserRepository;
import com.example.translationchat.common.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

    private UserService userService;

    @BeforeEach
    public void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("회원가입_성공")
    public void testSignUp_Success() {
        // given
        SignUpForm signUpForm = SignUpForm.builder()
            .email("test@email.com")
            .password("test123!")
            .name("test")
            .nationality("KOREA")
            .language("KO")
            .build();

        // when
        String result = userService.signUp(signUpForm);

        // then
        assertEquals("회원가입이 완료되었습니다.", result);
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
            .nationality("KOREA")
            .language("KO")
            .build();

        // when
        CustomException exception = assertThrows(
            CustomException.class, () -> userService.signUp(signUpForm));
        // then
        assertEquals(ALREADY_REGISTER_USER, exception.getErrorCode());
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
            .nationality("KOREA")
            .language("KO")
            .build();

        // when
        CustomException exception = assertThrows(
            CustomException.class, () -> userService.signUp(signUpForm));
        // then
        assertEquals(ALREADY_EXIST_NAME, exception.getErrorCode());
    }
}