package com.example.translationchat.chat.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.translationchat.chat.domain.model.ChatMessage;
import com.example.translationchat.chat.domain.model.ChatRoom;
import com.example.translationchat.chat.domain.model.ChatRoomUser;
import com.example.translationchat.chat.domain.repository.ChatMessageRepository;
import com.example.translationchat.chat.domain.repository.ChatRoomRepository;
import com.example.translationchat.chat.domain.request.ChatMessageRequest;
import com.example.translationchat.client.domain.model.User;
import com.example.translationchat.client.domain.type.ActiveStatus;
import com.example.translationchat.client.domain.type.Language;
import com.example.translationchat.common.kafka.Producers;
import com.example.translationchat.common.security.principal.PrincipalDetails;
import com.example.translationchat.common.util.PapagoUtil;
import com.example.translationchat.server.handler.ChatHandler;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.WebSocketSession;

@SpringBootTest
public class ChatMessageServiceTest {
    @Mock
    private Producers producers;

    @Mock
    private PapagoUtil papagoUtil;

    @Mock
    private ChatHandler chatHandler;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Test
    @DisplayName("채팅 메세지 보내기 - 성공")
    public void testSendMessage() {
        // given
        User user = User.builder()
            .id(1L)
            .name("user")
            .language(Language.en)
            .status(ActiveStatus.ONLINE)
            .build();
        User otherUser = User.builder()
            .id(2L)
            .name("otherUser")
            .language(Language.ko)
            .status(ActiveStatus.ONLINE)
            .build();
        ChatRoom room = ChatRoom.builder()
            .id(10L)
            .title("requester 님과 user 님의 대화")
            .build();
        ChatRoomUser roomUser = ChatRoomUser.builder()
            .user(user)
            .chatRoom(room)
            .build();
        ChatRoomUser roomOtherUser = ChatRoomUser.builder()
            .user(otherUser)
            .chatRoom(room)
            .build();
        List<ChatRoomUser> chatRoomUsers = Arrays.asList(roomUser, roomOtherUser);
        room.setChatRoomUsers(chatRoomUsers);

        String originalMessage = "Hello, World!";
        String translatedMessage = "안녕하세요, 세계!";

        WebSocketSession mockSession = mock(WebSocketSession.class);
        when(mockSession.isOpen()).thenReturn(true);
        when(chatHandler.getRoomIdSession(anyLong())).thenReturn(Arrays.asList(mockSession, mockSession));

        when(chatRoomRepository.findById(anyLong())).thenReturn(Optional.of(room));
        when(papagoUtil.getTransSentence(anyString(), any(), any())).thenReturn(translatedMessage);
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(new ChatMessage());

        // Act
        ChatMessageService chatMessageService = new ChatMessageService(producers, papagoUtil, chatHandler, chatRoomRepository, chatMessageRepository);
        chatMessageService.sendMessage(createMockAuthentication(user), room.getId(), new ChatMessageRequest(originalMessage));

        // Assert
        verify(chatMessageRepository, times(1)).save(any(ChatMessage.class));
        verify(producers, times(1)).produceMessage(anyLong(), anyString());
    }

    private Authentication createMockAuthentication(User user) {
        return new UsernamePasswordAuthenticationToken(new PrincipalDetails(user), null);
    }
}