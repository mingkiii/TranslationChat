package com.example.translationchat.client.controller;

import com.example.translationchat.client.domain.dto.FriendInfoDto;
import com.example.translationchat.client.domain.type.FriendshipStatus;
import com.example.translationchat.client.service.FriendService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ws/friend")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    // 친구 요청
    @PostMapping("/request")
    public ResponseEntity<String> requestFriend(
        Authentication authentication, @RequestParam String friendName
    ) {
        return ResponseEntity.ok(
            friendService.requestFriend(authentication, friendName)
        );
    }

    // 친구 요청 수락
    @PostMapping("/accept")
    public ResponseEntity<String> requestAccept(
        Authentication authentication,
        @RequestParam Long notificationId,
        @RequestParam String requesterName
    ) {
        return ResponseEntity.ok(friendService.acceptFriendship(
            authentication, notificationId, requesterName)
        );
    }

    // 친구 요청 거절
    @PostMapping("/refuse")
    public ResponseEntity<String> requestRefuse(
        Authentication authentication,
        @RequestParam Long notificationId,
        @RequestParam String requesterName
    ) {
        return ResponseEntity.ok(friendService.refuseFriendship(
            authentication, notificationId, requesterName)
        );
    }

    // 친구 차단
    @PutMapping("/block")
    public ResponseEntity<String> block(Authentication authentication, @RequestParam String friendName) {
        return ResponseEntity.ok(friendService.block(authentication, friendName));
    }

    // 차단 해제
    @PutMapping("/unblock")
    public ResponseEntity<String> unblock(Authentication authentication, @RequestParam String friendName) {
        return ResponseEntity.ok(friendService.unBlock(authentication, friendName));
    }

    // 친구관계인 친구목록 조회
    @GetMapping("/friendship-friends")
    public ResponseEntity<Set<FriendInfoDto>> friends(Authentication authentication) {
        return ResponseEntity.ok(friendService.getFriends(
            authentication, FriendshipStatus.ACCEPTED)
        );
    }
    // 친구요청한 친구목록 조회
    @GetMapping("/request-friends")
    public ResponseEntity<Set<FriendInfoDto>> requestFriends(Authentication authentication) {
        return ResponseEntity.ok(friendService.getFriends(
            authentication, FriendshipStatus.PENDING)
        );
    }
    // 차단한 친구목록 조회
    @GetMapping("/blocked-friends")
    public ResponseEntity<Set<FriendInfoDto>> blockedFriends(Authentication authentication) {
        return ResponseEntity.ok(friendService.getFriends(
            authentication, FriendshipStatus.BLOCKED)
        );
    }

    // 친구 삭제
    @DeleteMapping
    public void delete(Authentication authentication, @RequestParam String friendName) {
        friendService.delete(authentication, friendName);
    }
}
