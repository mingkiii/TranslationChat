package com.example.translationchat.client.service;

import static com.example.translationchat.common.exception.ErrorCode.NOT_FOUND_NOTIFICATION;

import com.example.translationchat.client.domain.dto.NotificationDto;
import com.example.translationchat.client.domain.form.NotificationForm;
import com.example.translationchat.client.domain.model.Notification;
import com.example.translationchat.client.domain.model.User;
import com.example.translationchat.client.domain.repository.NotificationRepository;
import com.example.translationchat.common.exception.CustomException;
import com.example.translationchat.common.security.principal.PrincipalDetails;
import com.example.translationchat.server.handler.EchoHandler;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EchoHandler echoHandler;
    private final NotificationRepository notificationRepository;

    // 알림을 생성하고, 알림을 받는 유저에게 웹소켓을 통해 알림메세지를 보낸다.
    @Async // 비동기적으로 처리
    public void create(NotificationForm form, String message) {
        notificationRepository.save(Notification.builder()
            .user(form.getUser())
            .args(form.getArgs())
            .content(form.getContentType())
            .build());

        sendNotificationMessage(form.getUser().getId(), message);
    }

    // 새로운 알림이 생성될 때 호출되는 메서드
    public void sendNotificationMessage(Long userId, String message) {
        WebSocketSession session = echoHandler.getUserSession(userId);

        if (session != null && session.isOpen()) {
            try {
                TextMessage textMessage = new TextMessage(message);
                session.sendMessage(textMessage);
            } catch (IOException e) {
                // 메시지 전송 중 오류 발생시 처리
                e.printStackTrace();
            }
        }
    }

    // 알림을 읽은 경우 삭제 (대화 요청알람의 경우 유저가 수락/거절 할 경우 읽은 경우로 간주하여 삭제)
    public void delete(Long id) {
        notificationRepository.findById(id)
            .ifPresent(notificationRepository::delete);
    }

    public NotificationDto getNotificationDto(Long id) {
        return notificationRepository.findById(id)
            .map(NotificationDto::from)
            .orElseThrow(() -> new CustomException(NOT_FOUND_NOTIFICATION));
    }

    // 알림 목록 조회 - 페이징 처리
    public Page<NotificationDto> unreadNotifications(
        Authentication authentication, Pageable pageable
    ) {
        User user = getUser(authentication);
        Page<Notification> notifications = notificationRepository.findAllByUser(
            user, pageable);

        return notifications.map(NotificationDto::from);
    }

    private User getUser(Authentication authentication) {
        PrincipalDetails details = (PrincipalDetails) authentication.getPrincipal();
        return details.getUser();
    }
}
