package com.example.translationchat.domain.chat.controller;

import com.example.translationchat.chat.domain.dto.ChatRoomDto;
import com.example.translationchat.common.security.principal.PrincipalDetails;
import com.example.translationchat.domain.chat.service.ChatRoomUserService;
import com.example.translationchat.domain.user.entity.User;
import com.example.translationchat.domain.user.service.UserService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomUserService roomUserService;
    private final UserService userService;

    // 대화방 목록 조회
    @GetMapping
    public ResponseEntity<List<ChatRoomDto>> getChatRooms(
        @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        User user = userService.getUserByEmail(principalDetails.getEmail());

        List<ChatRoomDto> chatRoomList = roomUserService.findAllByUserId(user.getId())
            .stream()
            .map(roomUser -> ChatRoomDto.from(roomUser.getChatRoom()))
            .collect(Collectors.toList());

        return ResponseEntity.ok(chatRoomList);
    }

    @PostMapping
    public
}
