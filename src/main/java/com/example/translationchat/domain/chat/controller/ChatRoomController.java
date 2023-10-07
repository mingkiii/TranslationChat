package com.example.translationchat.domain.chat.controller;

import com.example.translationchat.domain.chat.dto.ChatRoomDto;
import com.example.translationchat.common.security.principal.PrincipalDetails;
import com.example.translationchat.domain.chat.entity.ChatRoom;
import com.example.translationchat.domain.chat.entity.ChatRoomUser;
import com.example.translationchat.domain.chat.service.ChatMessageService;
import com.example.translationchat.domain.chat.service.ChatRoomService;
import com.example.translationchat.domain.chat.service.ChatRoomUserService;
import com.example.translationchat.domain.user.entity.User;
import com.example.translationchat.domain.user.service.UserService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomUserService roomUserService;
    private final ChatRoomService chatRoomService;
    private final ChatMessageService messageService;
    private final UserService userService;

    // 대화방 목록 조회
    @GetMapping
    public ResponseEntity<List<ChatRoomDto>> ChatRooms(
        @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        User user = userService.getUserByEmail(principalDetails.getEmail());

        List<ChatRoomDto> chatRoomList = roomUserService.findAllByUserId(user.getId())
            .stream()
            .map(roomUser -> ChatRoomDto.from(roomUser.getChatRoom()))
            .collect(Collectors.toList());

        return ResponseEntity.ok(chatRoomList);
    }

    @PostMapping("/users/{targetUserId}")
    public ResponseEntity<ChatRoomDto> createChatRoom(
        @AuthenticationPrincipal PrincipalDetails principalDetails,
        @PathVariable("targetUserId") Long targetUserId
    ) {
        User user = userService.getUserByEmail(principalDetails.getEmail());
        User targetUser = userService.findById(targetUserId);
        ChatRoom room = chatRoomService.findByTitleOrCreateRoom(user, targetUser);
        roomUserService.enterChatRoom(room, user, targetUser);

        return ResponseEntity.ok(ChatRoomDto.from(room));
    }

    @DeleteMapping("/{roomId}")
    public void exitChatRoom(
        @AuthenticationPrincipal PrincipalDetails principalDetails,
        @PathVariable("roomId") Long roomId
    ) {
        User user = userService.getUserByEmail(principalDetails.getEmail());
        ChatRoom chatRoom = chatRoomService.findByRoomId(roomId);

        roomUserService.exit(chatRoom, user);

        List<ChatRoomUser> chatRoomUsers = roomUserService.findByRoomId(roomId);
        if (chatRoomUsers.isEmpty()) {
            messageService.deleteOfChatRoomId(roomId);
            chatRoomService.delete(chatRoom);
        }
    }

}
