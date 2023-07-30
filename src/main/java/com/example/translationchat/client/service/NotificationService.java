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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
    public NotificationDto create(NotificationForm form) {
        User user = form.getUser();
        Notification notification = Notification.builder()
            .user(user)
            .args(form.getArgs())
            .content(form.getContentType())
            .build();
        Notification saveNotification = notificationRepository.save(notification);

        // 생성된 알림 메시지를 WebSocket을 통해 해당 유저에게 전달
        String message = saveNotification.getArgs()+ "," + saveNotification.getContent().getDisplayName();
        createNotification(user.getName(), message);
        return NotificationDto.from(saveNotification);
    }

    // 새로운 알림이 생성될 때 호출되는 메서드
    public void createNotification(String userName, String message) {
        WebSocketSession session = echoHandler.getUserSession(userName);

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

    // 요청 했던 알람을 삭제하기 위해
    public void delete(NotificationForm form) {
        Notification notification = notificationRepository.findByArgsAndContent(
                form.getArgs(), form.getContentType())
            .orElseThrow(
                () -> new CustomException(NOT_FOUND_NOTIFICATION));
        notificationRepository.delete(notification);
    }

    // 알림을 읽은 경우 삭제 (친구 요청알람의 경우 유저가 수락/거절 할 경우 읽은 경우로 간주하여 삭제)
    public void delete(Long id) {
        Optional<Notification> optionalNotification = notificationRepository.findById(id);
        if (optionalNotification.isPresent()) {
            Notification notification = optionalNotification.get();

            notificationRepository.delete(notification);
        }
    }

    // 알림 목록 조회
    public List<NotificationDto> unreadNotifications(Authentication authentication) {
        User user = getUser(authentication);
        List<Notification> notifications = notificationRepository.findAllByUser(user);
        return notifications.stream()
            .map(NotificationDto::from)
            .collect(Collectors.toList());
    }

    // 알림 갯수 조회
    public Long unreadNotificationCount(PrincipalDetails principal) {
        User user = principal.getUser();
        return notificationRepository.countByUser(user);
    }

    private User getUser(Authentication authentication) {
        PrincipalDetails details = (PrincipalDetails) authentication.getPrincipal();
        return details.getUser();
    }
}
