package com.example.translationchat.chat.controller;

import com.example.translationchat.chat.service.RandomChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/random/")
@RequiredArgsConstructor
public class RandomChatController {

    private final RandomChatService randomChatService;

    @MessageMapping("/room/create")
    @SendTo("/sub/random/room/create")
    public ResponseEntity<String> createRoom(Authentication authentication) {
        return ResponseEntity.ok(randomChatService.createRoom(authentication));
    }

    @PostMapping("/room/join")
    public void joinRoom(Authentication authentication) {
        randomChatService.joinRandomChat(authentication);
    }
}
