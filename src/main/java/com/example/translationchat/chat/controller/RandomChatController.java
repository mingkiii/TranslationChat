package com.example.translationchat.chat.controller;

import com.example.translationchat.chat.domain.request.RandomChatMessageRequest;
import com.example.translationchat.chat.service.RandomChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/random/chat")
@RequiredArgsConstructor
public class RandomChatController {

    private final RandomChatService randomChatService;

    // 랜덤 채팅 시작 -> 매칭 후 방 생성
    @PostMapping("/room/join")
    public void joinRoom(Authentication authentication) {
        randomChatService.joinQueue(authentication);
    }

    // 방 나가기 -> 상대에게 안내메세지 보내고, 방 삭제
    @DeleteMapping("/room/{roomId}/out")
    public void outRoom(Authentication authentication, @PathVariable Long roomId) {
        randomChatService.outRoom(authentication, roomId);
    }

    // 메시지 보내기
    @MessageMapping("/send")
    public void sendMessage(Authentication authentication, @Payload Long roomId, RandomChatMessageRequest messageRequest) {
        randomChatService.sendMessage(authentication, roomId, messageRequest);
    }
}
