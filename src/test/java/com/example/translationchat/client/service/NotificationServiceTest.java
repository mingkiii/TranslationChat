package com.example.translationchat.client.service;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class NotificationServiceTest {
//    @Mock
//    private EchoHandler echoHandler;
//
//    @Mock
//    private NotificationRepository notificationRepository;
//
//    @InjectMocks
//    private NotificationService notificationService;
//
//    @Test
//    public void testCreateNotification() throws IOException {
//        // given
//        String senderName = "sender";
//        User user = User.builder()
//            .id(1L)
//            .name("user")
//            .email("user1@example.com")
//            .build();
//        NotificationForm form = NotificationForm.builder()
//            .user(user)
//            .args(senderName)
//            .contentType(ContentType.SUCCESS_FRIENDSHIP)
//            .build();
//        Notification notification = Notification.builder()
//            .id(10L)
//            .user(user)
//            .args(form.getArgs())
//            .content(form.getContentType())
//            .build();
//        WebSocketSession session = mock(WebSocketSession.class);
//        when(echoHandler.getUserSession(any())).thenReturn(session);
//        when(session.isOpen()).thenReturn(true);
//        when(notificationRepository.save(any())).thenReturn(notification);
//
//        // when
//        NotificationDto result = notificationService.create(form);
//
//        // then
//        assertEquals(10L, result.getId());
//        assertEquals(senderName, result.getArgs());
//        assertEquals(ContentType.SUCCESS_FRIENDSHIP.getDisplayName(), result.getContent());
//        verify(session, times(1)).sendMessage(any(TextMessage.class));
//    }
//
//    @Test
//    @DisplayName("알림 목록 조회")
//    public void testUnreadNotifications() {
//        // given
//        User user = User.builder()
//            .id(1L)
//            .name("user")
//            .email("user1@example.com")
//            .build();
//
//        Notification notification1 = Notification.builder()
//            .id(10L)
//            .user(user)
//            .args("friend1")
//            .content(ContentType.REFUSE_FRIEND_REQUEST)
//            .build();
//        Notification notification2 = Notification.builder()
//            .id(15L)
//            .user(user)
//            .args("friend2")
//            .content(ContentType.SUCCESS_FRIENDSHIP)
//            .build();
//        List<Notification> notifications = new ArrayList<>();
//        notifications.add(notification1);
//        notifications.add(notification2);
//
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<Notification> notificationPage = new PageImpl<>(notifications, pageable, notifications.size());
//        when(notificationRepository.findAllByUser(user, pageable)).thenReturn(notificationPage);
//        // when
//        Page<NotificationDto> result =
//            notificationService.unreadNotifications(createMockAuthentication(user), pageable);
//
//        // then
//        assertEquals(2, result.getContent().size());
//    }
//
//    private Authentication createMockAuthentication(User user) {
//        return new UsernamePasswordAuthenticationToken(new PrincipalDetails(user), null);
//    }
}