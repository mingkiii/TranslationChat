package com.example.translationchat.chat.controller;

import com.example.translationchat.chat.domain.dto.ChatRoomDto;
import com.example.translationchat.domain.chat.service.ChatRoomUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.WebSocketSession;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRoomUserController {

    private final ChatRoomUserService chatRoomUserService;

    // 대화 요청, 대화방 생성, 요청 알림 생성(roomId 값 넣음)
    @PostMapping("/request")
    public void request(Authentication authentication, WebSocketSession session, @RequestParam("id") Long receiverUserId) {
        chatRoomUserService.request(authentication, session, receiverUserId);
    }

    // 받은 대화 요청 알림을 통해 수락할 경우 -> 대화방 정보 저장, kafka 토픽 구독, 해당 알림 삭제
    @PostMapping("/room/accept")
    public void accept(Authentication authentication, WebSocketSession session, @RequestParam("id") Long notificationId) {
        chatRoomUserService.accept(authentication, session, notificationId);
    }

    // 받은 대화 요청 알림을 통해 거절할 경우 - 해당 알림 삭제, 방 삭제, 요청자에게 거절 알림 생성
    @DeleteMapping
    public void refuse(Authentication authentication, @RequestParam("id") Long notificationId) {
        chatRoomUserService.refuse(authentication, notificationId);
    }

    // 대화방 목록 조회
    @GetMapping("/rooms")
    public ResponseEntity<Page<ChatRoomDto>> getUserRooms(Authentication authentication, Pageable pageable) {
        return ResponseEntity.ok(chatRoomUserService.getUserRooms(authentication, pageable));
    }

    // 대화방 나가기
    @DeleteMapping("/room/out/{roomId}")
    public void outRoom(Authentication authentication, @PathVariable Long roomId) {
        chatRoomUserService.outRoom(authentication, roomId);
    }
}

