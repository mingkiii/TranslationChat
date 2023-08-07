package com.example.translationchat.client.controller;

import com.example.translationchat.client.domain.dto.NotificationDto;
import com.example.translationchat.client.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/notifications")
    public ResponseEntity<Page<NotificationDto>> unreadNotifications(
        Authentication authentication, Pageable pageable
    ) {
        return ResponseEntity.ok(
            notificationService.unreadNotifications(authentication, pageable)
        );
    }

    // 요청 읽음/ 요청 수락,거절 -> 알림을 읽은 의미로 해당 알림 삭제
    @DeleteMapping
    public void delete(@RequestParam("id") Long notificationId) {
        notificationService.delete(notificationId);
    }
}
