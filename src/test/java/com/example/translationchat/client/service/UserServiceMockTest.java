package com.example.translationchat.client.service;

import static com.example.translationchat.common.exception.ErrorCode.ALREADY_REGISTERED_EMAIL;
import static com.example.translationchat.common.exception.ErrorCode.ALREADY_REGISTERED_NAME;
import static com.example.translationchat.common.exception.ErrorCode.LOGIN_FAIL;
import static com.example.translationchat.common.exception.ErrorCode.LOGIN_REQUIRED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
public class UserServiceMockTest {
    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtAuthenticationProvider provider;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserLockService userLockService;
    @Mock
    private AuthenticationManager authenticationManager;

    // 회원가입 테스트
    @Test
    @DisplayName("회원가입_성공")
    public void testSignUp_Success() {
        // given
        SignUpForm signUpForm = SignUpForm.builder()
            .email("test@email.com")
            .password("test123!")
            .name("test")
            .nationality("KOREA")
            .language("french")
            .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByName(anyString())).thenReturn(false);
        when(userLockService.isAvailableEmail(anyString(),anyString())).thenReturn(true);
        when(userLockService.isAvailableName(anyString(),anyString())).thenReturn(true);

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

        SignUpForm signUpForm = SignUpForm.builder()
            .email(existingUser.getEmail())
            .password("test123!")
            .name("test")
            .nationality("KOREA")
            .language("KO")
            .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(true);
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
            .name("existing")
            .nationality(Nationality.KOREA)
            .language(Language.KO)
            .build();

        SignUpForm signUpForm = SignUpForm.builder()
            .email("new@email.com")
            .password("test123!")
            .name(existingUser.getName())
            .nationality("KOREA")
            .language("KO")
            .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByName(anyString())).thenReturn(true);
        when(userLockService.isAvailableEmail(anyString(),anyString())).thenReturn(true);
        when(userLockService.isAvailableName(anyString(),anyString())).thenReturn(false);
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

        when(authenticationManager.authenticate(any())).thenReturn(authentication(user));
        when(provider.createToken(user.getId(), user.getEmail())).thenReturn("testToken");

        // when
        String token = userService.login(form);

        // then
        assertEquals("testToken", token);
    }
    private Authentication authentication(User user) {
        PrincipalDetails principalDetails = new PrincipalDetails(user);
        return new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());
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
        // 로그인 실패로 인증 객체는 null 반환
        when(authenticationManager.authenticate(authenticationToken)).thenReturn(null);

        // when
        CustomException exception = assertThrows(CustomException.class,
            () -> userService.login(form));

        // then
        assertEquals(LOGIN_FAIL, exception.getErrorCode());
    }

    @Test
    @DisplayName("회원 탈퇴 - 성공")
    void testDelete_Success() {
        // given
        String userEmail = "test@test.com";
        User user = User.builder()
            .email(userEmail)
            .build();
        PrincipalDetails principalDetails = new PrincipalDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, "", principalDetails.getAuthorities());
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        // when
        userService.delete(authentication);

        // then
        verify(userRepository, times(1)).findByEmail(userEmail);
        verify(userRepository, times(1)).delete(user);
    }
    @Test
    @DisplayName("회원 탈퇴 - 실패: 로그인 후 토큰이 만료된 경우")
    void testDelete_Fail_LoginRequired() {
        // given
        Authentication authentication = null; // 로그인 후 토큰이 만료된 경우

        // when, then
        CustomException exception = assertThrows(CustomException.class,
            () -> userService.delete(authentication));
        assertEquals(LOGIN_REQUIRED, exception.getErrorCode());
    }

    @Test
    @DisplayName("회원 정보 조회 - 성공")
    void testGetInfo_Success() {
        // given
        String userEmail = "test@test.com";
        User user = User.builder()
            .email(userEmail)
            .name("Test User")
            .nationality(Nationality.UK)
            .language(Language.FR)
            .randomApproval(true)
            .build();
        PrincipalDetails principalDetails = new PrincipalDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, "", principalDetails.getAuthorities());
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        // when
        UserInfoDto userInfo = userService.getInfo(authentication);

        // then
        assertNotNull(userInfo);
        assertEquals(userEmail, userInfo.getEmail());
        assertEquals("Test User", userInfo.getName());
        assertEquals(String.valueOf(Nationality.UK), userInfo.getNationality());
        assertEquals(Language.FR.getDisplayName(), userInfo.getLanguage());
        assertTrue(userInfo.isRandomApproval());
    }
    @Test
    @DisplayName("회원 정보 조회 - 실패: 로그인 후 토큰이 만료된 경우")
    void testGetInfo_Fail_LoginRequired() {
        // given
        Authentication authentication = null; // 로그인 후 토큰이 만료된 경우

        // when, then
        CustomException exception = assertThrows(CustomException.class,
            () -> userService.getInfo(authentication));
        assertEquals(LOGIN_REQUIRED, exception.getErrorCode());
    }

    @Test
    @DisplayName("회원 정보 수정 - 성공")
    void testUpdateInfo_Success() {
        // given
        String userEmail = "test@test.com";
        String userPassword = "oldPassword";
        User user = User.builder()
            .email(userEmail)
            .password(userPassword)
            .name("OldUser")
            .language(Language.EN)
            .build();
        UpdateUserForm form = UpdateUserForm.builder()
            .name("Updated")
            .password("newPassword")
            .nationality("FRANCE")
            .language("English")
            .build();
        PrincipalDetails principalDetails = new PrincipalDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, "", principalDetails.getAuthorities());
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(form.getPassword())).thenReturn("encodedPassword");
        when(userLockService.isAvailableName(anyString(), anyString())).thenReturn(true);
        when(userRepository.existsByName(anyString())).thenReturn(false);
        // when
        UserInfoDto updatedUserInfo = userService.updateInfo(authentication, form);

        // then
        assertNotNull(updatedUserInfo);
        assertEquals("Updated", updatedUserInfo.getName());
        assertEquals("FRANCE", String.valueOf(updatedUserInfo.getNationality()));
        assertEquals("English", updatedUserInfo.getLanguage());
    }
}
