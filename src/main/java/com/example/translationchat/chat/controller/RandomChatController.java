package com.example.translationchat.chat.controller;

import com.example.translationchat.chat.domain.request.RandomChatMessageRequest;
import com.example.translationchat.chat.service.RandomChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/room/create")
    public ResponseEntity<String> createRoom(Authentication authentication) {
        return ResponseEntity.ok(randomChatService.createRoom(authentication));
    }

    @PostMapping("/room/join")
    public void joinRoom(Authentication authentication) {
        randomChatService.joinRandomChat(authentication);
    }

    @DeleteMapping("/room/{roomId}/out")
    public void outRoom(Authentication authentication, @PathVariable Long roomId) {
        randomChatService.outRoom(authentication, roomId);
    }

    @MessageMapping("/send")
    public void sendMessage(Authentication authentication, @Payload Long roomId, RandomChatMessageRequest messageRequest) {
        randomChatService.sendMessage(authentication, roomId, messageRequest);
    }
}
