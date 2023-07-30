package com.example.translationchat.client.controller;

import com.example.translationchat.client.domain.dto.NotificationDto;
import com.example.translationchat.client.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ws/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/unread-notifications")
    public ResponseEntity<List<NotificationDto>> unreadNotifications(
        Authentication authentication) {
        return ResponseEntity.ok(notificationService.unreadNotifications(authentication));
    }
}
