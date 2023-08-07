package com.example.translationchat.chat.controller;

import com.example.translationchat.chat.service.ChatRoomUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/chat")
@RequiredArgsConstructor
public class ChatRoomUserController {

    private final ChatRoomUserService chatRoomUserService;

    // 대화 요청, 대화방 생성
    @PostMapping("/request")
    public void request(Authentication authentication, @RequestParam("id") Long receiverUserId) {
        chatRoomUserService.request(authentication, receiverUserId);
    }

    // 대화 요청 알림을 통해 수락할 경우 -> 대화방 정보 저장, kafka 토픽 구독, 해당 알림 삭제
    @PostMapping("/accept")
    public void accept(Authentication authentication, @RequestParam("id") Long notificationId) {
        chatRoomUserService.accept(authentication, notificationId);
    }

    // 대화 요청 알림을 통해 거절할 경우 - 해당 알림 삭제, 요청자에게 거절 알림 생성
    @DeleteMapping
    public void refuse(Authentication authentication, @RequestParam("id") Long notificationId) {
        chatRoomUserService.refuse(authentication, notificationId);
    }
}

