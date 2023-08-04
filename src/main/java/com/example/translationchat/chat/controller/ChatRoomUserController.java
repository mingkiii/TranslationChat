package com.example.translationchat.chat.controller;

import com.example.translationchat.chat.service.ChatRoomUserService;
import com.example.translationchat.client.domain.dto.NotificationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatRoomUserController {

    private final ChatRoomUserService chatRoomUserService;

    @PostMapping("/request")
    public void request(Authentication authentication, @RequestParam Long receiverUerId) {
        chatRoomUserService.request(authentication, receiverUerId);
    }

    @DeleteMapping
    public void refuse(Authentication authentication, NotificationDto notificationDto) {
        chatRoomUserService.refuse(authentication, notificationDto);
    }
}

