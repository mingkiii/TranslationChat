package com.example.translationchat.chat.service;

import static com.example.translationchat.common.exception.ErrorCode.NOT_FOUND_USER;
import static com.example.translationchat.common.exception.ErrorCode.OFFLINE_USER;

import com.example.translationchat.chat.domain.model.ChatRoomUser;
import com.example.translationchat.chat.domain.model.ChatRoom;
import com.example.translationchat.chat.domain.repository.ChatRoomUserRepository;
import com.example.translationchat.chat.domain.repository.ChatRoomRepository;
import com.example.translationchat.client.domain.dto.NotificationDto;
import com.example.translationchat.client.domain.model.User;
import com.example.translationchat.client.domain.repository.UserRepository;
import com.example.translationchat.client.domain.type.ActiveStatus;
import com.example.translationchat.client.service.NotificationService;
import com.example.translationchat.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ChatRoomRepository roomRepository;
    private final UserRepository userRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final NotificationService notificationService;

    // 대화 요청 알림을 통해 수락 시 대화방 생성
    @Transactional
    public void create(Long notificationId) {
        NotificationDto notificationDto = notificationService.getNotificationDto(notificationId);

        User user = notificationDto.getUser();
        User requester = userRepository.findById(notificationDto.getArgs())
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER));
        // 요청자 접속 중인지 확인
        if (ActiveStatus.OFFLINE == requester.getStatus()) {
            throw new CustomException(OFFLINE_USER);
        }

        // 대화방 생성
        ChatRoom room = roomRepository.save(ChatRoom.builder()
            .title(String.format("%s 님과 %s 님의 대화", user.getName(), requester.getName()))
            .build());

        // 대화방 정보(유저,방) 저장
        createChatRoomUser(user, room);
        createChatRoomUser(requester, room);

        String topicName = "CHAT_ROOM" + room.getId();
        NewTopic newTopic = new NewTopic(topicName, 1, (short) 1);
        // Kafka Topic 에 구독자 추가
        kafkaTemplate.send(newTopic.name(), "Subscribed");

        // 대화 요청 알림 삭제
        notificationService.delete(notificationId);
    }

    private void createChatRoomUser(User user, ChatRoom room) {
        chatRoomUserRepository.save(ChatRoomUser.builder()
            .user(user)
            .chatRoom(room)
            .build());
    }
}
