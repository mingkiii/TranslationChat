package com.example.translationchat.domain.user.service;

import static com.example.translationchat.common.exception.ErrorCode.ALREADY_REGISTERED_EMAIL;
import static com.example.translationchat.common.exception.ErrorCode.ALREADY_REGISTERED_NAME;
import static com.example.translationchat.common.exception.ErrorCode.LOGIN_FAIL;
import static com.example.translationchat.common.exception.ErrorCode.NEW_PASSWORD_MISMATCH_RE_PASSWORD;
import static com.example.translationchat.common.exception.ErrorCode.USER_PASSWORD_EQUALS_NEW_PASSWORD;
import static com.example.translationchat.common.exception.ErrorCode.USER_PASSWORD_MISMATCH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.translationchat.common.exception.CustomException;
import com.example.translationchat.common.security.JwtAuthenticationProvider;
import com.example.translationchat.common.security.principal.PrincipalDetails;
import com.example.translationchat.domain.type.Language;
import com.example.translationchat.domain.type.Nationality;
import com.example.translationchat.domain.user.entity.User;
import com.example.translationchat.domain.user.form.LoginForm;
import com.example.translationchat.domain.user.form.SignUpForm;
import com.example.translationchat.domain.user.form.UpdatePasswordForm;
import com.example.translationchat.domain.user.form.UpdateUserForm;
import com.example.translationchat.domain.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserServiceTest {
    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtAuthenticationProvider provider;
    @Mock
    private AuthenticationManager authenticationManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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
            .language(Language.fr)
            .build();

        when(userRepository.existsByEmail(signUpForm.getEmail())).thenReturn(false);
        when(userRepository.existsByName(signUpForm.getName())).thenReturn(false);
        // when
        String result = userService.signUp(signUpForm);
        // then
        assertEquals("회원가입이 완료되었습니다.", result);
    }

    @Test
    @DisplayName("회원가입_실패-이메일 중복")
    public void testSignUp_DuplicateEmail() {
        // given
        SignUpForm signUpForm = SignUpForm.builder()
            .email("existEmail") // Use existing email to test duplicate
            .password("test123!")
            .name("test")
            .nationality(Nationality.UK)
            .language(Language.fr)
            .build();

        when(userRepository.existsByEmail(signUpForm.getEmail())).thenReturn(true);
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
        SignUpForm signUpForm = SignUpForm.builder()
            .email("new@email.com")
            .password("test123!")
            .name("existing")
            .nationality(Nationality.UK)
            .language(Language.fr)
            .build();

        when(userRepository.existsByName(signUpForm.getName())).thenReturn(true);
        // when
        CustomException exception = assertThrows(
            CustomException.class, () -> userService.signUp(signUpForm));
        // then
        assertEquals(ALREADY_REGISTERED_NAME, exception.getErrorCode());
    }

    // 로그인 테스트
    @Test
    @DisplayName("로그인_성공")
    void testLogin_Success() {
        // given
        User user = User.builder()
            .id(1L)
            .email("test@example.com")
            .password("encodedPassword")
            .build();
        LoginForm form = LoginForm.builder()
            .email("test@example.com")
            .password("test123!")
            .build();
        PrincipalDetails principalDetails = new PrincipalDetails(user.getEmail(), user.getPassword());
        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(form.getEmail(), form.getPassword());
        Authentication authentication = mock(Authentication.class);

        when(userRepository.findByEmail(form.getEmail())).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(authenticationToken)).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principalDetails);
        when(provider.createToken(user)).thenReturn("test_token");

        // when
        String token = userService.login(form);

        // then
        assertEquals("test_token", token);
    }

    @Test
    @DisplayName("로그인_실패")
    void testLogin_Fail_NOT_FOUND_USER() {
        // given
        LoginForm form = LoginForm.builder()
            .email("test@example.com")
            .password("test123!")
            .build();
        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(form.getEmail(), form.getPassword());
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(authenticationToken)).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // when
        CustomException exception = assertThrows(CustomException.class,
            () -> userService.login(form));

        // then
        assertEquals(LOGIN_FAIL, exception.getErrorCode());
    }

    @Test
    @DisplayName("회원 정보 수정 - 성공")
    void testUpdateInfo_Success() {
        // given
        String userEmail = "test@test.com";
        String userPassword = "oldPassword";
        User user = User.builder()
            .id(1L)
            .email(userEmail)
            .password(userPassword)
            .name("OldUser")
            .nationality(Nationality.CANADA)
            .language(Language.en)
            .randomApproval(true)
            .build();
        UpdateUserForm form = UpdateUserForm.builder()
            .nationality(Nationality.UK)
            .language(Language.ko)
            .build();

        // when
        userService.updateInfo(user, form);

        // then
        assertEquals(form.getNationality(), user.getNationality());
        assertEquals(form.getLanguage(), user.getLanguage());
    }

    @Test
    @DisplayName("비밀번호 변경 - 성공")
    void testUpdatePassword() {
        //given
        UpdatePasswordForm form = UpdatePasswordForm.builder()
            .password("test1234!")
            .newPassword("test1212@")
            .rePassword("test1212@")
            .build();
        String userPassword = passwordEncoder.encode(form.getPassword());
        User user = User.builder()
            .email("test@test.com")
            .password(userPassword)
            .build();

        when(passwordEncoder.matches(form.getPassword(), user.getPassword())).thenReturn(true);
        //when
        userService.updatePassword(user, form);
        //then
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("비밀번호 변경 - 실패_실제 비밀번호 != 입력 비밀번호")
    void testUpdatePassword_Fail_USER_PASSWORD_MISMATCH() {
        //given
        UpdatePasswordForm form = UpdatePasswordForm.builder()
            .password("test1234!")
            .newPassword("test1212@")
            .rePassword("test1212@")
            .build();
        User user = User.builder()
            .email("test@test.com")
            .password("adf1234!")
            .build();

        when(passwordEncoder.matches(form.getPassword(), user.getPassword())).thenReturn(false);
        //when
        CustomException exception = assertThrows(CustomException.class,
            () -> userService.updatePassword(user, form));

        // then
        assertEquals(USER_PASSWORD_MISMATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("비밀번호 변경 - 실패_새 비밀번호 != 새 비밀번호 확인")
    void testUpdatePassword_Fail_NEW_PASSWORD_MISMATCH_RE_PASSWORD() {
        //given
        UpdatePasswordForm form = UpdatePasswordForm.builder()
            .password("test1234!")
            .newPassword("bbb1234!")
            .rePassword("aaa1234!")
            .build();
        String userPassword = passwordEncoder.encode(form.getPassword());
        User user = User.builder()
            .email("test@test.com")
            .password(userPassword)
            .build();

        when(passwordEncoder.matches(form.getPassword(), user.getPassword())).thenReturn(true);
        //when
        CustomException exception = assertThrows(CustomException.class,
            () -> userService.updatePassword(user, form));

        // then
        assertEquals(NEW_PASSWORD_MISMATCH_RE_PASSWORD, exception.getErrorCode());
    }

    @Test
    @DisplayName("비밀번호 변경 - 실패_기존 비밀번호 == 새 비밀번호")
    void testUpdatePassword_Fail_USER_PASSWORD_EQUALS_NEW_PASSWORD() {
        //given
        UpdatePasswordForm form = UpdatePasswordForm.builder()
            .password("test1234!")
            .newPassword("test1234!")
            .rePassword("test1234!")
            .build();
        String userPassword = passwordEncoder.encode(form.getPassword());
        User user = User.builder()
            .email("test@test.com")
            .password(userPassword)
            .build();

        when(passwordEncoder.matches(form.getPassword(), user.getPassword())).thenReturn(true);
        //when
        CustomException exception = assertThrows(CustomException.class,
            () -> userService.updatePassword(user, form));

        // then
        assertEquals(USER_PASSWORD_EQUALS_NEW_PASSWORD, exception.getErrorCode());
    }
}