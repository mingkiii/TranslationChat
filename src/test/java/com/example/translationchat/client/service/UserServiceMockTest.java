package com.example.translationchat.client.service;

import static com.example.translationchat.common.exception.ErrorCode.ALREADY_REGISTERED_NAME;
import static com.example.translationchat.common.exception.ErrorCode.LOGIN_FAIL;
import static com.example.translationchat.common.exception.ErrorCode.NOT_FOUND_USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.translationchat.client.domain.dto.MyInfoDto;
import com.example.translationchat.client.domain.dto.UserInfoDto;
import com.example.translationchat.client.domain.form.LoginForm;
import com.example.translationchat.client.domain.form.UpdateUserForm;
import com.example.translationchat.client.domain.model.User;
import com.example.translationchat.client.domain.repository.UserRepository;
import com.example.translationchat.client.domain.type.Language;
import com.example.translationchat.client.domain.type.Nationality;
import com.example.translationchat.common.exception.CustomException;
import com.example.translationchat.common.redis.util.RedisLockUtil;
import com.example.translationchat.common.security.JwtAuthenticationProvider;
import com.example.translationchat.common.security.principal.PrincipalDetails;
import com.example.translationchat.server.handler.EchoHandler;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
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
import org.springframework.web.socket.WebSocketSession;

@SpringBootTest
public class UserServiceMockTest {
    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtAuthenticationProvider provider;
    @Mock
    private RedisLockUtil redisLockUtil;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private NotificationService notificationService;
    @Mock
    private EchoHandler echoHandler;

    // 로그인 테스트
    @Test
    @DisplayName("로그인_성공")
    void testLogin_Success() throws IOException {
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
        PrincipalDetails principalDetails = new PrincipalDetails(user);
        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(form.getEmail(), form.getPassword());
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(authenticationToken)).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principalDetails);
        WebSocketSession session = mock(WebSocketSession.class);
        when(echoHandler.getUserSession(user.getName())).thenReturn(session);
        when(notificationService.unreadNotificationCount(principalDetails)).thenReturn(3L);
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
    @DisplayName("회원 탈퇴 - 성공")
    void testDelete_Success() {
        // given
        String userEmail = "test@test.com";
        User user = User.builder()
            .email(userEmail)
            .build();
        PrincipalDetails principalDetails = new PrincipalDetails(user);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principalDetails);

        // when
        userService.delete(authentication);

        // then
        verify(userRepository, times(1)).delete(user);
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

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        // when
        MyInfoDto userInfo = userService.getInfo(createMockAuthentication(user));

        // then
        assertNotNull(userInfo);
        assertEquals(userEmail, userInfo.getEmail());
        assertEquals("Test User", userInfo.getName());
        assertEquals(Nationality.UK, userInfo.getNationality());
        assertEquals(Language.FR.getDisplayName(), userInfo.getLanguage());
        assertTrue(userInfo.isRandomApproval());
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
            .nationality(Nationality.CANADA)
            .language(Language.EN)
            .build();
        UpdateUserForm form = UpdateUserForm.builder()
            .name("Updated")
            .password("newPassword")
            .nationality(Nationality.UK)
            .language(Language.FR)
            .build();

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(form.getPassword())).thenReturn("encodedPassword");
        when(userRepository.existsByName(form.getName())).thenReturn(false);
        when(redisLockUtil.getLock(anyString(), anyLong())).thenReturn(true);
        // when
        MyInfoDto updatedUserInfo = userService.updateInfo(createMockAuthentication(user), form);

        // then
        assertNotNull(updatedUserInfo);
        assertEquals("Updated", updatedUserInfo.getName());
        assertEquals(form.getNationality(), updatedUserInfo.getNationality());
        assertEquals(form.getLanguage().getDisplayName(), updatedUserInfo.getLanguage());
    }
    @Test
    @DisplayName("회원 정보 수정 - 실패_이미 등록된 이름")
    void testUpdateInfo_Fail() {
        // given
        String userEmail = "test@test.com";
        String userPassword = "oldPassword";
        User user = User.builder()
            .email(userEmail)
            .password(userPassword)
            .name("OldUser")
            .nationality(Nationality.CANADA)
            .language(Language.EN)
            .build();
        UpdateUserForm form = UpdateUserForm.builder()
            .name("Updated")
            .password("newPassword")
            .nationality(Nationality.UK)
            .language(Language.FR)
            .build();
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(form.getPassword())).thenReturn("encodedPassword");
        when(userRepository.existsByName(form.getName())).thenReturn(true);
        when(redisLockUtil.getLock(anyString(), anyLong())).thenReturn(true);
        // when
        CustomException exception = assertThrows(CustomException.class,
            () ->userService.updateInfo(createMockAuthentication(user), form));

        // then
        assertEquals(ALREADY_REGISTERED_NAME, exception.getErrorCode());
    }

    @Test
    @DisplayName("유저 검색 - 성공")
    void searchByUserName_Success() {
        //given
        String name = "min";
        User user1 = User.builder()
            .name("kim_minHo")
            .nationality(Nationality.UK)
            .language(Language.FR)
            .build();
        User user2 = User.builder()
            .name("park_minSu")
            .nationality(Nationality.KOREA)
            .language(Language.KO)
            .build();
        List<User> users = Arrays.asList(user1, user2);

        when(userRepository.searchByName(name)).thenReturn(users);
        //when
        List<UserInfoDto> result = userService.searchByUserName(name);
        //then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("kim_minHo");
        assertThat(result.get(1).getName()).isEqualTo("park_minSu");

        verify(userRepository, times(1)).searchByName(name);
    }

    @Test
    @DisplayName("유저 검색 - 실패")
    void testSearchByUserName_Fail_UsersNotFound() {
        // given
        String name = "John";
        when(userRepository.searchByName(name)).thenReturn(List.of());
        // when
        CustomException exception = assertThrows(CustomException.class,
            () -> userService.searchByUserName(name));
        // then
        assertEquals(NOT_FOUND_USER, exception.getErrorCode());
        verify(userRepository, times(1)).searchByName(name);
    }
    private Authentication createMockAuthentication(User user) {
        return new UsernamePasswordAuthenticationToken(new PrincipalDetails(user), null);
    }
}
