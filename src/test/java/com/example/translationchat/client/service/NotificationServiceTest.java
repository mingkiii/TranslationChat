package com.example.translationchat.client.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.translationchat.client.domain.dto.NotificationDto;
import com.example.translationchat.client.domain.form.NotificationForm;
import com.example.translationchat.client.domain.model.Notification;
import com.example.translationchat.client.domain.model.User;
import com.example.translationchat.client.domain.repository.NotificationRepository;
import com.example.translationchat.client.domain.type.ContentType;
import com.example.translationchat.common.security.principal.PrincipalDetails;
import com.example.translationchat.server.handler.EchoHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@SpringBootTest
public class NotificationServiceTest {
    @Mock
    private EchoHandler echoHandler;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    public void testCreateNotification() throws IOException {
        // given
        String senderName = "sender";
        User user = User.builder()
            .id(1L)
            .name("user")
            .email("user1@example.com")
            .build();
        NotificationForm form = NotificationForm.builder()
            .user(user)
            .args(senderName)
            .contentType(ContentType.SUCCESS_FRIENDSHIP)
            .build();
        Notification notification = Notification.builder()
            .id(10L)
            .user(user)
            .args(form.getArgs())
            .content(form.getContentType())
            .build();
        WebSocketSession session = mock(WebSocketSession.class);
        when(echoHandler.getUserSession(any())).thenReturn(session);
        when(session.isOpen()).thenReturn(true);
        when(notificationRepository.save(any())).thenReturn(notification);

        // when
        NotificationDto result = notificationService.create(form);

        // then
        assertEquals(10L, result.getId());
        assertEquals(senderName, result.getArgs());
        assertEquals(ContentType.SUCCESS_FRIENDSHIP.getDisplayName(), result.getContent());
        verify(session, times(1)).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("알림 목록 조회")
    public void testUnreadNotifications() {
        // given
        User user = User.builder()
            .id(1L)
            .name("user")
            .email("user1@example.com")
            .build();

        Notification notification1 = Notification.builder()
            .id(10L)
            .user(user)
            .args("friend1")
            .content(ContentType.REFUSE_FRIEND_REQUEST)
            .build();
        Notification notification2 = Notification.builder()
            .id(15L)
            .user(user)
            .args("friend2")
            .content(ContentType.SUCCESS_FRIENDSHIP)
            .build();
        List<Notification> notifications = new ArrayList<>();
        notifications.add(notification1);
        notifications.add(notification2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> notificationPage = new PageImpl<>(notifications, pageable, notifications.size());
        when(notificationRepository.findAllByUser(user, pageable)).thenReturn(notificationPage);
        // when
        Page<NotificationDto> result =
            notificationService.unreadNotifications(createMockAuthentication(user), pageable);

        // then
        assertEquals(2, result.getContent().size());
    }

    private Authentication createMockAuthentication(User user) {
        return new UsernamePasswordAuthenticationToken(new PrincipalDetails(user), null);
    }
}