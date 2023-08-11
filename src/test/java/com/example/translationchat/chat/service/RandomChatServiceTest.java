package com.example.translationchat.chat.service;

import static com.example.translationchat.common.exception.ErrorCode.ALREADY_RANDOM_CHAT_ROOM;
import static com.example.translationchat.common.exception.ErrorCode.RANDOM_CHAT_UNAVAILABLE_STATUS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.translationchat.chat.domain.model.RandomChatRoom;
import com.example.translationchat.chat.domain.repository.RandomChatRoomRepository;
import com.example.translationchat.chat.domain.request.RandomChatMessageRequest;
import com.example.translationchat.client.domain.model.User;
import com.example.translationchat.client.domain.type.ActiveStatus;
import com.example.translationchat.client.domain.type.Language;
import com.example.translationchat.client.service.NotificationService;
import com.example.translationchat.client.service.ReportService;
import com.example.translationchat.common.exception.CustomException;
import com.example.translationchat.common.papago.PapagoService;
import com.example.translationchat.common.redis.util.RedisService;
import com.example.translationchat.common.security.principal.PrincipalDetails;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringBootTest
@SpringJUnitConfig
public class RandomChatServiceTest {
    @InjectMocks
    private RandomChatService randomChatService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private RandomChatRoomRepository randomChatRoomRepository;

    @Mock
    private RedisService redisService;

    @Mock
    private PapagoService papagoService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ReportService reportService;

    @Test
    @DisplayName("랜덤채팅방 매칭 후 참여 - 성공")
    public void testJoinRandomChat() {
        //given
        User user1 = User.builder()
            .id(1L)
            .name("user")
            .status(ActiveStatus.ONLINE)
            .randomApproval(true)
            .build();
        User user2 = User.builder()
            .id(1L)
            .name("user")
            .randomApproval(true)
            .status(ActiveStatus.ONLINE)
            .build();
        String key = "random_chat_queue";

        when(randomChatRoomRepository.existsByJoinUser1OrJoinUser2(any(User.class), any(User.class))).thenReturn(false);
        when(redisService.getLock(anyString(), anyLong())).thenReturn(true);
        when(redisService.pop(key)).thenReturn(user1);
        when(redisService.pop(key)).thenReturn(user2);
        //when
        randomChatService.joinRandomChat(createMockAuthentication(user1));
        //then
        verify(redisService, times(3)).getLock(anyString(), anyLong());
        verify(redisService, times(3)).unLock(anyString());
        verify(randomChatRoomRepository, times(1)).save(any(RandomChatRoom.class));
    }

    @Test
    @DisplayName("랜덤채팅방 탐색 후 참여 - 실패_랜덤 채팅 이용 불가 상태")
    public void testJoinRandomChat_Fail_RANDOM_CHAT_UNAVAILABLE_STATUS() {
        //given
        User user = User.builder()
            .id(1L)
            .name("user")
            .randomApproval(false)
            .build();
        when(reportService.isReportDateOlderThanAWeek(user)).thenReturn(false);
        //when
        CustomException exception = assertThrows(CustomException.class,
            () -> randomChatService.joinRandomChat(createMockAuthentication(user)));
        //then
        assertEquals(RANDOM_CHAT_UNAVAILABLE_STATUS, exception.getErrorCode());
    }

    @Test
    @DisplayName("랜덤채팅방 탐색 후 참여 - 실패_이미 랜덤 채팅 참여 상태")
    public void testJoinRandomChat_Fail_ALREADY_RANDOM_CHAT_ROOM() {
        //given
        User user = User.builder()
            .id(1L)
            .name("user")
            .randomApproval(true)
            .build();
        when(randomChatRoomRepository.existsByJoinUser1OrJoinUser2(user, user)).thenReturn(true);
        //when
        CustomException exception = assertThrows(CustomException.class,
            () -> randomChatService.joinRandomChat(createMockAuthentication(user)));
        //then
        assertEquals(ALREADY_RANDOM_CHAT_ROOM, exception.getErrorCode());
    }

    @Test
    @DisplayName("방 나가기 - 성공")
    public void testOutRoom() {
        //given
        RandomChatRoom room = RandomChatRoom.builder()
            .id(1L)
            .joinUser1(User.builder()
                .id(1L)
                .build())
            .joinUser2(User.builder()
                .id(2L)
                .build())
            .build();
        when(randomChatRoomRepository.findById(anyLong())).thenReturn(Optional.of(room));
        //when
        randomChatService.outRoom(createMockAuthentication(any(User.class)), 1L);
        //then
        verify(randomChatRoomRepository, times(1)).delete(room);
        verify(notificationService, times(1)).sendNotificationMessage(anyLong(),anyString());
    }

    @Test
        @DisplayName("채팅 메시지 보내기 - 성공")
    public void testSendMessage() {
        //given
        RandomChatMessageRequest request = new RandomChatMessageRequest("안녕하세요");
        User user = User.builder()
            .id(1L)
            .language(Language.ko)
            .build();
        User otherUser = User.builder()
            .id(2L)
            .language(Language.en)
            .build();
        RandomChatRoom room = RandomChatRoom.builder()
            .id(1L)
            .joinUser1(user)
            .joinUser2(otherUser)
            .build();

        when(randomChatRoomRepository.findById(anyLong())).thenReturn(Optional.of(room));
        when(papagoService.getTransSentence(request.getContent(),user.getLanguage(), otherUser.getLanguage())).thenReturn("Hello");
        //when
        randomChatService.sendMessage(createMockAuthentication(user), 1L, request);
        //then
        verify(messagingTemplate, times(1)).convertAndSend(eq("/sub/random/chat/" + room.getId()), anyString());
    }

    private Authentication createMockAuthentication(User user) {
        return new UsernamePasswordAuthenticationToken(new PrincipalDetails(user), null);
    }
}