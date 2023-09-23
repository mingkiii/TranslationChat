package com.example.translationchat.client.controller;

import com.example.translationchat.client.domain.dto.NotificationDto;
import com.example.translationchat.client.domain.model.Notification;
import com.example.translationchat.client.domain.model.User;
import com.example.translationchat.client.service.NotificationService;
import com.example.translationchat.client.service.UserService;
import com.example.translationchat.common.security.principal.PrincipalDetails;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/v1/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    // 알람 sse 구독
    @GetMapping(value = "/subscribe/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe(
        @PathVariable(name = "userId") Long userId,
        @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId
    ) {
        return ResponseEntity.ok(notificationService.subscribe(userId, lastEventId));
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationDto>> notifications(
        @AuthenticationPrincipal PrincipalDetails principal
    ) {
        User user = userService.getUserByEmail(principal.getUsername());
        List<Notification> notifications = notificationService.getAlarms(user.getId());
        List<NotificationDto> alarmDtoList = notifications.stream()
            .map(NotificationDto::from)
            .collect(Collectors.toList());

        return ResponseEntity.ok(alarmDtoList);
    }

    // 요청 읽음/ 요청 수락,거절 -> 알림을 읽은 의미로 해당 알림 삭제
    @DeleteMapping("/{notificationId}")
    public void delete(@AuthenticationPrincipal PrincipalDetails principal,
        @PathVariable("notificationId") Long notificationId) {
        User user = userService.getUserByEmail(principal.getUsername());
        notificationService.delete(user.getId(), notificationId);
    }
}
