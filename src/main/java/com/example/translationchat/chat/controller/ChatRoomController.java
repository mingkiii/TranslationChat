package com.example.translationchat.chat.controller;

import com.example.translationchat.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService roomService;

    // 대화 요청 알림을 통해 수락할 경우 -> 대화방 생성, 해당 알림 삭제
    @PostMapping("/room")
    public void create(@RequestParam("id") Long notificationId) {
        roomService.create(notificationId);
    }
}
