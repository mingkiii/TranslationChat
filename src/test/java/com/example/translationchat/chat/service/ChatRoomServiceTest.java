package com.example.translationchat.chat.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.translationchat.chat.domain.model.ChatRoom;
import com.example.translationchat.chat.domain.model.ChatRoomUser;
import com.example.translationchat.chat.domain.repository.ChatRoomRepository;
import com.example.translationchat.chat.domain.repository.ChatRoomUserRepository;
import com.example.translationchat.client.domain.dto.NotificationDto;
import com.example.translationchat.client.domain.model.User;
import com.example.translationchat.client.domain.repository.UserRepository;
import com.example.translationchat.client.domain.type.ActiveStatus;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootTest
class ChatRoomServiceTest {
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ChatRoomRepository roomRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChatRoomUserRepository chatRoomUserRepository;

    @InjectMocks
    private ChatRoomService chatRoomService;

    @Test
    public void testCreateChatRoom() {
        // given
        User user1 = User.builder()
            .id(1L)
            .name("user1")
            .status(ActiveStatus.ONLINE)
            .build();
        User user2 = User.builder()
            .id(2L)
            .name("user2")
            .status(ActiveStatus.ONLINE)
            .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));

        ChatRoom chatRoom = ChatRoom.builder()
            .id(1L)
            .title("user1 님과 user2 님의 대화방")
            .build();
        when(roomRepository.save(any(ChatRoom.class))).thenReturn(chatRoom);

        NotificationDto notificationDto = NotificationDto.builder()
            .id(10L)
            .user(user1)
            .args(user2.getId())
            .build();
        // when
        chatRoomService.create(notificationDto);

        // 대화방 생성에 대한 검증
        verify(roomRepository, times(1)).save(any(ChatRoom.class));

        // chatRoomUserRepository의 save 메서드가 두 번 호출되는지 검증
        verify(chatRoomUserRepository, times(2)).save(any(ChatRoomUser.class));

        // kafkaTemplate의 send 메서드가 호출되는지 검증
        verify(kafkaTemplate, times(1)).send(anyString(), anyString());
    }
}