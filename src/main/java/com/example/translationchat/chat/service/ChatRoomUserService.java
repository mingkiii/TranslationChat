package com.example.translationchat.chat.service;

import static com.example.translationchat.common.exception.ErrorCode.ALREADY_EXISTS_ROOM;
import static com.example.translationchat.common.exception.ErrorCode.ALREADY_REQUEST;
import static com.example.translationchat.common.exception.ErrorCode.ALREADY_REQUEST_RECEIVER;
import static com.example.translationchat.common.exception.ErrorCode.NOT_FOUND_USER;
import static com.example.translationchat.common.exception.ErrorCode.OFFLINE_USER;
import static com.example.translationchat.common.exception.ErrorCode.USER_IS_BLOCKED;

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
import com.example.translationchat.common.security.principal.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRoomUserService {

    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final NotificationService notificationService;
    private final ChatRoomUserRepository chatRoomUserRepository;
    // 대화 요청
    public void request(Authentication authentication, Long receiverUserId) {
        User user = getUser(authentication);
        User receiver = userRepository.findById(receiverUserId)
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

        // 상대가 오프라인 상태인 경우
        if (ActiveStatus.ONLINE != receiver.getStatus()) {
            throw new CustomException(OFFLINE_USER);
        }

        // 유저가 차단한 유저인지 확인 -> 차단한 경우 요청되지 않음
        if (favoriteRepository.findByUserAndFavoriteUser(user, receiver)
                .map(Favorite::isBlocked).orElse(false)) {
            throw new CustomException(USER_IS_BLOCKED);
        }

        // 상대가 유저를 차단한 상태인지 확인
        // 차단 당한 상태일 경우 - 오프라인 상태 예외발생으로 요청 되지 않도록 함.
        if (favoriteRepository.findByUserAndFavoriteUser(receiver, user)
                .map(Favorite::isBlocked).orElse(false)) {
            throw new CustomException(OFFLINE_USER);
        }

        // 요청받는 유저가 이미 요청자에게 대화 요청한 경우
        if (notificationService.existsNotification(receiver, user.getId(), ContentType.REQUEST_CHAT)) {
            throw new CustomException(ALREADY_REQUEST_RECEIVER);
        }

        // 요청자가 이미 요청한 경우 예외 발생
        if (notificationService.existsNotification(user, receiverUserId, ContentType.REQUEST_CHAT)) {
            throw new CustomException(ALREADY_REQUEST);
        }

        // 이미 대화방이 있는지 확인
        if (chatRoomUserRepository.existsByUser(user, receiver)) {
            throw new CustomException(ALREADY_EXISTS_ROOM);
        }

        // 요청받는 유저에게 대화 요청 알림 생성
        String message = String.format("%s 님이 %s 님에게 %s",
            user.getName(), receiver.getName(),
            ContentType.REQUEST_CHAT.getDisplayName());

        notificationService.create(NotificationForm.builder()
                                    .user(receiver)
                                    .args(user.getId())
                                    .contentType(ContentType.REQUEST_CHAT)
                                    .build(), message);
    }

    // 대화 요청 거절
    public void refuse(Long notificationId) {
        NotificationDto notificationDto = notificationService.getNotificationDto(notificationId);
        User user = notificationDto.getUser();
        User requester = userRepository.findById(notificationDto.getArgs())
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

        String message = String.format("%s 님이 %s 님의 %s",
            user.getName(), requester.getName(),
            ContentType.REFUSE_REQUEST_CHAT.getDisplayName());

        // 요청자에게 요청 거절 알림 생성
        notificationService.create(NotificationForm.builder()
            .user(requester)
            .args(user.getId())
            .contentType(ContentType.REFUSE_REQUEST_CHAT)
            .build(), message);

        // 대화 요청 알림 삭제
        notificationService.delete(notificationId);
    }

    private User getUser(Authentication authentication) {
        PrincipalDetails details = (PrincipalDetails) authentication.getPrincipal();
        return details.getUser();
    }
}
