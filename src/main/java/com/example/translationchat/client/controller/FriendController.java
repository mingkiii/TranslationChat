package com.example.translationchat.client.controller;

import com.example.translationchat.client.domain.dto.FriendInfoDto;
import com.example.translationchat.client.domain.form.NotificationForm;
import com.example.translationchat.client.domain.type.ContentType;
import com.example.translationchat.client.domain.type.FriendshipStatus;
import com.example.translationchat.client.service.FriendService;
import com.example.translationchat.client.service.NotificationService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ws/friend")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;
    private final NotificationService notificationService;

    // 친구 요청
    @PostMapping("/request")
    public ResponseEntity<String> requestFriend(Authentication authentication, @RequestParam String friendName) {

        NotificationForm form = friendService.requestFriend(authentication, friendName);
        // 상대방에게 친구요청 알림 생성
        notificationService.create(form);
        return ResponseEntity.ok(friendName + ContentType.FRIEND_REQUEST.getDisplayName());
    }

    // 친구 요청 수락
    @PostMapping("/accept")
    public ResponseEntity<String> requestAccept(
        Authentication authentication,
        @RequestParam Long notificationId,
        @RequestParam String requesterName
    ) {
        NotificationForm form = friendService.acceptFriendship(authentication, requesterName);
        notificationService.create(form);
        // 요청에 대한 알림을 읽을 걸로 간주하여 해당 알림 삭제합니다.
        notificationService.delete(notificationId);

        return ResponseEntity.ok(requesterName + ContentType.SUCCESS_FRIENDSHIP);
    }

    // 친구 요청 거절
    @PostMapping("/refuse")
    public ResponseEntity<String> requestRefuse(
        Authentication authentication,
        @RequestParam Long notificationId,
        @RequestParam String requesterName
    ) {
        NotificationForm form = friendService.refuseFriendship(authentication, requesterName);
        if (form.getContentType() == ContentType.RECEIVE_REFUSE_REQUEST) {
            // 요청한 유저에게 거절의 알림 생성
            notificationService.create(form);
        }else {
            // requester 에게 유저가 요청한 알림 삭제
            notificationService.delete(form);
        }
        // 요청에 대한 알림을 읽을 걸로 간주하여 해당 알림 삭제합니다.
        notificationService.delete(notificationId);
        return ResponseEntity.ok(requesterName + ContentType.REFUSE_REQUEST);
    }

    // 친구 차단
    @PostMapping("/block")
    public ResponseEntity<String> block(Authentication authentication, @RequestParam String friendName) {
        return ResponseEntity.ok(friendService.block(authentication, friendName));
    }

    // 차단 해제
    @PostMapping("/unblock")
    public ResponseEntity<String> unblock(Authentication authentication, @RequestParam String friendName) {
        return ResponseEntity.ok(friendService.unBlock(authentication, friendName));
    }

    // 친구관계인 친구목록 조회
    @GetMapping("/friendship-friends")
    public ResponseEntity<Set<FriendInfoDto>> friends(Authentication authentication) {
        return ResponseEntity.ok(friendService.getFriends(authentication, FriendshipStatus.ACCEPTED));
    }
    // 친구요청한 친구목록 조회
    @GetMapping("/request-friends")
    public ResponseEntity<Set<FriendInfoDto>> requestFriends(Authentication authentication) {
        return ResponseEntity.ok(friendService.getFriends(authentication, FriendshipStatus.PENDING));
    }
    // 차단한 친구목록 조회
    @GetMapping("/blocked-friends")
    public ResponseEntity<Set<FriendInfoDto>> blockedFriends(Authentication authentication) {
        return ResponseEntity.ok(friendService.getFriends(authentication, FriendshipStatus.BLOCKED));
    }
}
