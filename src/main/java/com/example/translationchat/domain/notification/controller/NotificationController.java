package com.example.translationchat.domain.notification.controller;

import static com.example.translationchat.common.exception.ErrorCode.BAD_REQUEST;

import com.example.translationchat.common.exception.CustomException;
import com.example.translationchat.common.security.principal.PrincipalDetails;
import com.example.translationchat.domain.notification.dto.NotificationDto;
import com.example.translationchat.domain.notification.entity.Notification;
import com.example.translationchat.domain.notification.service.NotificationService;
import com.example.translationchat.domain.user.entity.User;
import com.example.translationchat.domain.user.service.UserService;
import java.util.List;
import java.util.Objects;
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

    @DeleteMapping("/{notificationId}")
    public void delete(@AuthenticationPrincipal PrincipalDetails principal,
        @PathVariable("notificationId") Long notificationId) {
        User user = userService.getUserByEmail(principal.getUsername());
        Notification notification = notificationService.findById(notificationId);
        if (!Objects.equals(notification.getUser().getId(), user.getId())) {
            throw new CustomException(BAD_REQUEST);
        }
        notificationService.delete(notification);
    }
}
