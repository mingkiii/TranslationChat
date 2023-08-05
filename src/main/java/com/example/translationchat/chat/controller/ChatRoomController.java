package com.example.translationchat.chat.controller;

import com.example.translationchat.chat.service.ChatRoomService;
import com.example.translationchat.client.domain.dto.NotificationDto;
import com.example.translationchat.client.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService roomService;
    private final NotificationService notificationService;

    // 대화 요청 수락 -> 대화방 생성, 해당 알림 삭제
    @PostMapping("/room")
    public void create(@RequestParam Long notificationId) {
        NotificationDto notificationDto = notificationService.getNotificationDto(notificationId);
        roomService.create(notificationDto);
        notificationService.delete(notificationId);
    }
}
