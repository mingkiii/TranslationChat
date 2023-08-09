package com.example.translationchat.chat.service;

import static com.example.translationchat.common.exception.ErrorCode.ALREADY_RANDOM_CHAT_ROOM;
import static com.example.translationchat.common.exception.ErrorCode.NOT_FOUND_RANDOM_CHAT_ROOM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import com.example.translationchat.client.domain.type.Language;
import com.example.translationchat.client.service.NotificationService;
import com.example.translationchat.common.exception.CustomException;
import com.example.translationchat.common.papago.PapagoService;
import com.example.translationchat.common.redis.util.RedisLockUtil;
import com.example.translationchat.common.security.principal.PrincipalDetails;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@SpringBootTest
public class RandomChatServiceTest {
    @InjectMocks
    private RandomChatService randomChatService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private RandomChatRoomRepository randomChatRoomRepository;

    @Mock
    private RedisLockUtil redisLockUtil;

    @Mock
    private PapagoService papagoService;

    @Mock
    private NotificationService notificationService;

    @Test
    @DisplayName("랜덤채팅방 생성 - 성공")
    public void testCreateRoom() {
        //given
        User user = User.builder()
            .id(1L)
            .name("user")
            .build();
        when(redisLockUtil.getLock(anyString(), anyLong())).thenReturn(true);
        //when
        String result = randomChatService.createRoom(createMockAuthentication(user));
        //then
        assertNotNull(result);
        assertEquals("새로운 랜덤 채팅방을 생성하였습니다.", result);

        verify(randomChatRoomRepository, times(1)).save(any(RandomChatRoom.class));
        verify(redisLockUtil, times(1)).getLock(anyString(), anyLong());
        verify(redisLockUtil, times(1)).unLock(anyString());
    }

    @Test
    @DisplayName("랜덤채팅방 탐색 후 참여 - 성공")
    public void testJoinRandomChat() {
        //given
        User user = User.builder()
            .id(1L)
            .name("user")
            .build();
        User createUser = User.builder()
            .id(2L)
            .name("createUser")
            .build();
        RandomChatRoom room = RandomChatRoom.builder()
            .id(1L)
            .createUser(createUser)
            .build();
        when(randomChatRoomRepository.existsByCreateUserOrJoinUser(user, user)).thenReturn(false);
        when(redisLockUtil.getLock(anyString(), anyLong())).thenReturn(true);
        when(randomChatRoomRepository.findFirstByJoinUserIsNull()).thenReturn(Optional.of(room));
        //when
        randomChatService.joinRandomChat(createMockAuthentication(user));
        //then
        verify(randomChatRoomRepository, times(1)).save(any(RandomChatRoom.class));
        verify(redisLockUtil, times(1)).getLock(anyString(), anyLong());
        verify(redisLockUtil, times(1)).unLock(anyString());
    }
    @Test
    @DisplayName("랜덤채팅방 탐색 후 참여 - 실패_이미 랜덤 채팅 참여 상태")
    public void testJoinRandomChat_Fail_ALREADY_RANDOM_CHAT_ROOM() {
        //given
        User user = User.builder()
            .id(1L)
            .name("user")
            .build();
        when(randomChatRoomRepository.existsByCreateUserOrJoinUser(user, user)).thenReturn(true);
        //when
        CustomException exception = assertThrows(CustomException.class,
            () -> randomChatService.joinRandomChat(createMockAuthentication(user)));
        //then
        assertEquals(ALREADY_RANDOM_CHAT_ROOM, exception.getErrorCode());
    }
    @Test
    @DisplayName("랜덤채팅방 탐색 후 참여 - 실패_참여가능한 방 없음")
    public void testJoinRandomChat_Fail_NOT_FOUND_RANDOM_CHAT_ROOM() {
        //given
        User user = User.builder()
            .id(1L)
            .name("user")
            .build();
        when(randomChatRoomRepository.existsByCreateUserOrJoinUser(user, user)).thenReturn(false);
        when(redisLockUtil.getLock(anyString(), anyLong())).thenReturn(true);
        when(randomChatRoomRepository.findFirstByJoinUserIsNull()).thenReturn(Optional.empty());
        //when
        CustomException exception = assertThrows(CustomException.class,
            () -> randomChatService.joinRandomChat(createMockAuthentication(user)));
        //then
        assertEquals(NOT_FOUND_RANDOM_CHAT_ROOM, exception.getErrorCode());
    }

    @Test
    @DisplayName("방 생성 5분 지난 방 삭제 - 성공")
    public void testCheckEmptyRooms() {
        //given
        RandomChatRoom room1 = RandomChatRoom.builder()
            .id(1L)
            .createUser(User.builder()
                .id(1L)
                .build())
            .createdTime(Instant.now().minus(10, ChronoUnit.MINUTES))
            .build();
        RandomChatRoom room2 = RandomChatRoom.builder()
            .id(2L)
            .createUser(User.builder()
                .id(2L)
                .build())
            .createdTime(Instant.now().minus(6, ChronoUnit.MINUTES))
            .build();
        List<RandomChatRoom> emptyRooms = Arrays.asList(room1, room2);
        when(randomChatRoomRepository.findByJoinUserIsNullAndCreatedTimeBefore(any(Instant.class)))
            .thenReturn(emptyRooms);
        //when
        randomChatService.checkEmptyRooms();
        //then
        verify(randomChatRoomRepository, times(1)).delete(room1);
        verify(randomChatRoomRepository, times(1)).delete(room2);
        verify(notificationService, times(2)).sendNotificationMessage(anyLong(), anyString());
    }

    @Test
    @DisplayName("방 나가기 - 성공")
    public void testOutRoom() {
        //given
        RandomChatRoom room = RandomChatRoom.builder()
            .id(1L)
            .createUser(User.builder()
                .id(1L)
                .build())
            .build();
        when(randomChatRoomRepository.findById(anyLong())).thenReturn(Optional.of(room));
        //when
        randomChatService.outRoom(createMockAuthentication(any(User.class)), 1L);
        //then
        verify(randomChatRoomRepository, times(1)).delete(room);
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
            .createUser(user)
            .joinUser(otherUser)
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