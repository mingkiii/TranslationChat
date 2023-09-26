package com.example.translationchat.chat.controller;

import com.example.translationchat.chat.domain.request.ChatMessageRequest;
import com.example.translationchat.domain.chat.service.ChatMessageService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/chat")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatService;

    @PostMapping("/msg/{roomId}")
    public void sendChat(
        Authentication authentication,
        @PathVariable Long roomId,
        @RequestBody @Valid ChatMessageRequest message
    ) {
        chatService.sendMessage(authentication, roomId, message);
    }

}
