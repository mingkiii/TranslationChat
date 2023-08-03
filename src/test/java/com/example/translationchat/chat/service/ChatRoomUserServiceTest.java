package com.example.translationchat.chat.service;

import static com.example.translationchat.common.exception.ErrorCode.OFFLINE_USER;
import static com.example.translationchat.common.exception.ErrorCode.USER_IS_BLOCKED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.translationchat.client.domain.form.NotificationForm;
import com.example.translationchat.client.domain.model.Favorite;
import com.example.translationchat.client.domain.model.User;
import com.example.translationchat.client.domain.repository.FavoriteRepository;
import com.example.translationchat.client.domain.repository.UserRepository;
import com.example.translationchat.client.domain.type.ActiveStatus;
import com.example.translationchat.client.service.NotificationService;
import com.example.translationchat.common.exception.CustomException;
import com.example.translationchat.common.security.principal.PrincipalDetails;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@SpringBootTest
class ChatRoomUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private FavoriteRepository favoriteRepository;

    @InjectMocks
    private ChatRoomUserService chatRoomUserService;

    @Test
    @DisplayName("대화 요청 - 성공")
    public void testRequest_Successful() {
        // given
        User sender = User.builder()
            .id(1L)
            .name("sender")
            .status(ActiveStatus.ONLINE)
            .build();

        User receiver = User.builder()
            .id(2L)
            .name("receiver")
            .status(ActiveStatus.ONLINE)
            .build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));

        // when
        chatRoomUserService.request(createMockAuthentication(sender), 2L);

        // then
        verify(notificationService).create(any(NotificationForm.class), anyString());
    }

    @Test
    @DisplayName("대화 요청 - 실패_상대가 오프라인 상태")
    void testRequest_ReceiverOffline() {
        //given
        User sender = User.builder()
            .id(1L)
            .name("sender")
            .status(ActiveStatus.ONLINE)
            .build();

        User receiver = User.builder()
            .id(2L)
            .name("receiver")
            .status(ActiveStatus.OFFLINE)
            .build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        // when
        CustomException exception = assertThrows(CustomException.class,
            () -> chatRoomUserService.request(createMockAuthentication(sender), 2L));
        //then
        assertEquals(OFFLINE_USER, exception.getErrorCode());
    }

    @Test
    @DisplayName("대화 요청 - 실패_유저가 상대를 차단한 상태")
    void testRequest_Fail_USER_IS_BLOCKED() {
        //given
        User user = User.builder()
            .id(1L)
            .name("sender")
            .status(ActiveStatus.ONLINE)
            .build();
        User receiver = User.builder()
            .id(2L)
            .name("receiver")
            .status(ActiveStatus.OFFLINE)
            .build();
        Favorite userFavorite = Favorite.builder()
            .user(user)
            .favoriteUser(receiver)
            .blocked(true)
            .build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(favoriteRepository.findByUserAndFavoriteUser(user, receiver))
            .thenReturn(Optional.of(userFavorite));
        // when
        CustomException exception = assertThrows(CustomException.class,
            () -> chatRoomUserService.request(createMockAuthentication(user), 2L));
        //then
        assertEquals(USER_IS_BLOCKED, exception.getErrorCode());
    }

    @Test
    @DisplayName("대화 요청 - 실패_유저가 차단 당한 상태")
    void testRequest_Fail_FAKE_OFFLINE_USER() {
        //given
        User user = User.builder()
            .id(1L)
            .name("sender")
            .status(ActiveStatus.ONLINE)
            .build();
        User receiver = User.builder()
            .id(2L)
            .name("receiver")
            .status(ActiveStatus.OFFLINE)
            .build();
        Favorite favorite = Favorite.builder()
            .user(receiver)
            .favoriteUser(user)
            .blocked(true)
            .build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(favoriteRepository.findByUserAndFavoriteUser(receiver, user))
            .thenReturn(Optional.of(favorite));
        // when
        CustomException exception = assertThrows(CustomException.class,
            () -> chatRoomUserService.request(createMockAuthentication(user), 2L));
        //then
        assertEquals(OFFLINE_USER, exception.getErrorCode());
    }

    private Authentication createMockAuthentication(User user) {
        return new UsernamePasswordAuthenticationToken(new PrincipalDetails(user), null);
    }
}