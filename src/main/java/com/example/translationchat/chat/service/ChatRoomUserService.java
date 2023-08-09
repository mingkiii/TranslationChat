package com.example.translationchat.chat.service;

import static com.example.translationchat.common.exception.ErrorCode.ALREADY_EXISTS_ROOM;
import static com.example.translationchat.common.exception.ErrorCode.ALREADY_REQUEST;
import static com.example.translationchat.common.exception.ErrorCode.ALREADY_REQUEST_RECEIVER;
import static com.example.translationchat.common.exception.ErrorCode.NOT_FOUND_USER;
import static com.example.translationchat.common.exception.ErrorCode.NOT_INVALID_ROOM;
import static com.example.translationchat.common.exception.ErrorCode.NOT_YOUR_NOTIFICATION;
import static com.example.translationchat.common.exception.ErrorCode.OFFLINE_USER;
import static com.example.translationchat.common.exception.ErrorCode.USER_IS_BLOCKED;

import com.example.translationchat.chat.domain.dto.ChatRoomDto;
import com.example.translationchat.chat.domain.model.ChatRoom;
import com.example.translationchat.chat.domain.model.ChatRoomUser;
import com.example.translationchat.chat.domain.repository.ChatMessageRepository;
import com.example.translationchat.chat.domain.repository.ChatRoomRepository;
import com.example.translationchat.chat.domain.repository.ChatRoomUserRepository;
import com.example.translationchat.client.domain.dto.NotificationDto;
import com.example.translationchat.client.domain.form.NotificationForm;
import com.example.translationchat.client.domain.model.Favorite;
import com.example.translationchat.client.domain.model.User;
import com.example.translationchat.client.domain.repository.FavoriteRepository;
import com.example.translationchat.client.domain.repository.UserRepository;
import com.example.translationchat.client.domain.type.ActiveStatus;
import com.example.translationchat.client.domain.type.ContentType;
import com.example.translationchat.client.service.NotificationService;
import com.example.translationchat.common.exception.CustomException;
import com.example.translationchat.common.kafka.Producers;
import com.example.translationchat.common.security.principal.PrincipalDetails;
import com.example.translationchat.server.handler.ChatHandler;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.WebSocketSession;

@Service
@RequiredArgsConstructor
public class ChatRoomUserService {
    
    private final ChatHandler chatHandler;
    private final Producers producers;

    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final NotificationService notificationService;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    // 대화 요청
    @Transactional
    public void request(Authentication authentication, WebSocketSession session, Long receiverUserId) {
        User user = getUser(authentication);
        User receiver = getUserById(receiverUserId);

        // 요청 가능한 상황인지 확인
        validateRequest(user, receiver);

        // 대화방 생성
        String title = String.format("%s 님과 %s 님의 대화", user.getName(), receiver.getName());
        ChatRoom room = chatRoomRepository.save(ChatRoom.builder().title(title).build());

        // 웹소켓에 대화방아이디 등록
        chatHandler.putRoomIdSession(session, room.getId());

        // Kafka Topic 키(roomId)에 구독자 추가
        producers.produceMessage(room.getId(), "Subscribed");

        // 요청받는 유저에게 대화 요청 알림 생성
        String message = String.format("%s 님이 %s 님에게 %s",
            user.getName(), receiver.getName(), ContentType.REQUEST_CHAT.getDisplayName());

        notificationService.create(NotificationForm.builder()
            .user(receiver)
            .args(user.getId())
            .roomId(room.getId())
            .contentType(ContentType.REQUEST_CHAT)
            .build(), message);
    }

    private void isOnline(User user) {
        if (ActiveStatus.ONLINE != user.getStatus()) {
            throw new CustomException(OFFLINE_USER);
        }
    }

    private void validateRequest(User user, User receiver) {
        // 상대가 접속중인지
        isOnline(receiver);

        // 유저가 차단한 유저인지 확인 -> 차단한 경우 요청되지 않음
        if (favoriteRepository.findByUserAndFavoriteUser(user, receiver)
            .map(Favorite::isBlocked).orElse(false)) {
            throw new CustomException(USER_IS_BLOCKED);
        }

        // 상대가 유저를 차단한 상태인지 확인
        // 차단 당한 상태일 경우 - 오프라인 상태 거짓 예외발생으로 요청 되지 않도록 함.
        if (favoriteRepository.findByUserAndFavoriteUser(receiver, user)
            .map(Favorite::isBlocked).orElse(false)) {
            throw new CustomException(OFFLINE_USER);
        }

        // 요청받는 유저가 이미 요청자에게 대화 요청한 경우
        if (notificationService.existsNotification(receiver, user.getId(), ContentType.REQUEST_CHAT)) {
            throw new CustomException(ALREADY_REQUEST_RECEIVER);
        }

        // 요청자가 이미 요청한 경우 예외 발생
        if (notificationService.existsNotification(user, receiver.getId(), ContentType.REQUEST_CHAT)) {
            throw new CustomException(ALREADY_REQUEST);
        }

        // 이미 대화방이 있는지 확인
        if (chatRoomUserRepository.existsByUser(user, receiver)) {
            throw new CustomException(ALREADY_EXISTS_ROOM);
        }
    }

    // 대화 요청 수락
    @Transactional
    public void accept(Authentication authentication, WebSocketSession session, Long notificationId) {
        NotificationDto notificationDto = notificationService.getNotificationDto(notificationId);
        User user = getUser(authentication);
        // 유저의 알림인지 확인
        validateNotificationUser(notificationDto.getUser(), user);

        User requester = getUserById(notificationDto.getArgs());
        // 요청자 접속 중인지 확인
        isOnline(requester);

        ChatRoom room = checkRoom(notificationDto.getRoomId(), user, requester);

        // 대화방 정보(유저,방) 저장
        createChatRoomUser(user, room);
        createChatRoomUser(requester, room);

        // 웹소켓에 등록된 대화방아이디 세션리스트에 세션 추가
        chatHandler.putRoomIdSession(session, room.getId());

        // Kafka Topic 키(roomId)에 구독자 추가
        producers.produceMessage(room.getId(), "Subscribed");

        // 대화 요청 알림 삭제
        notificationService.delete(notificationId);
    }

    private void validateNotificationUser(User user1, User user2) {
        if (!Objects.equals(user1.getId(), user2.getId())) {
            throw new CustomException(NOT_YOUR_NOTIFICATION);
        }
    }

    private void createChatRoomUser(User user, ChatRoom room) {
        chatRoomUserRepository.save(ChatRoomUser.builder()
            .user(user)
            .chatRoom(room)
            .build());
    }

    // 대화 요청 거절
    @Transactional
    public void refuse(Authentication authentication, Long notificationId) {
        NotificationDto notificationDto = notificationService.getNotificationDto(notificationId);
        User user = getUser(authentication);
        // 유저의 알림인지 확인
        validateNotificationUser(notificationDto.getUser(), user);

        User requester = getUserById(notificationDto.getArgs());

        // 요청 시 만든 대화방 삭제
        ChatRoom room = checkRoom(notificationDto.getRoomId(), user, requester);
        chatRoomRepository.delete(room);

        // 웹소켓에 등록된 대화방아이디 삭제
        chatHandler.deleteRoomId(room.getId());

        // 요청시 생성된 kafka topic 키(roomId) 구독 취소 메세지 남김.
        producers.produceMessage(room.getId(), "Unsubscribed");

        // 요청자에게 요청 거절 알림 생성
        String message = String.format("%s 님이 %s 님의 %s",
            user.getName(), requester.getName(),
            ContentType.REFUSE_REQUEST_CHAT.getDisplayName());

        notificationService.create(NotificationForm.builder()
            .user(requester)
            .args(user.getId())
            .contentType(ContentType.REFUSE_REQUEST_CHAT)
            .build(), message);

        // 대화 요청 알림 삭제
        notificationService.delete(notificationId);
    }

    private ChatRoom checkRoom(Long roomId, User user, User requester) {
        ChatRoom room = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new CustomException(NOT_INVALID_ROOM));
        String title = room.getTitle();
        String isValid = String.format("%s 님과 %s 님의 대화", requester.getName(), user.getName());
        if (!title.equals(isValid)) {
            throw new CustomException(NOT_INVALID_ROOM);
        }
        return room;
    }

    private User getUser(Authentication authentication) {
        PrincipalDetails details = (PrincipalDetails) authentication.getPrincipal();
        return details.getUser();
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER));
    }

    // 대화방 목록 조회
    public Page<ChatRoomDto> getUserRooms(
        Authentication authentication, Pageable pageable
    ) {
        User user = getUser(authentication);
        Page<ChatRoomUser> chatRoomUsers = chatRoomUserRepository.findAllByUser(user, pageable);
        return chatRoomUsers.map(
            chatRoomUser -> ChatRoomDto.from(chatRoomUser.getChatRoom())
        );
    }

    // 대화방 나가기
    public void outRoom(Authentication authentication, Long roomId) {
        User user = getUser(authentication);

        ChatRoom room = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new CustomException(NOT_INVALID_ROOM));

        ChatRoomUser chatRoomUser = chatRoomUserRepository.findByUserAndChatRoom(user, room)
            .orElseThrow(() -> new CustomException(NOT_INVALID_ROOM));

        chatRoomUserRepository.delete(chatRoomUser);

        // 대화방에 모두 나가기 한 경우 방, 메세지, kafka 키(roomId) 구독 취소 메세지 남김.
        if (room.getChatRoomUsers().size() == 0) {
            // 대화 메시지 삭제 로직 추가
            chatMessageRepository.deleteByChatRoom(room);
            chatRoomRepository.delete(room);
            producers.produceMessage(room.getId(), "Unsubscribed");
        }
    }
}
