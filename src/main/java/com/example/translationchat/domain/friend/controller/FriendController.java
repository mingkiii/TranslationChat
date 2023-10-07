package com.example.translationchat.domain.friend.controller;

import static com.example.translationchat.common.exception.ErrorCode.ALREADY_REGISTERED_BLOCKED;
import static com.example.translationchat.common.exception.ErrorCode.ALREADY_REGISTERED_FRIEND;
import static com.example.translationchat.common.exception.ErrorCode.ALREADY_REQUESTED_FRIEND;
import static com.example.translationchat.common.exception.ErrorCode.BAD_REQUEST;
import static com.example.translationchat.common.exception.ErrorCode.NOT_FOUND_USER;

import com.example.translationchat.common.exception.CustomException;
import com.example.translationchat.common.security.principal.PrincipalDetails;
import com.example.translationchat.domain.block.service.BlockService;
import com.example.translationchat.domain.friend.dto.FriendDto;
import com.example.translationchat.domain.friend.entity.Friend;
import com.example.translationchat.domain.friend.service.FriendService;
import com.example.translationchat.domain.type.ApplyStatus;
import com.example.translationchat.domain.user.entity.User;
import com.example.translationchat.domain.user.service.UserService;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;
    private final UserService userService;
    private final BlockService blockService;

    // 친구 목록 조회
    @GetMapping
    public ResponseEntity<List<FriendDto>> friends(@AuthenticationPrincipal PrincipalDetails principal) {
        User user = userService.getUserByEmail(principal.getEmail());
        return ResponseEntity.ok(friendService.friends(user.getId()).stream()
            .map(friend -> FriendDto.of(friend.getId(), friend.getFriendUser()))
            .collect(Collectors.toList()));
    }

    // 유저가 받은 요청 목록 조회
    @GetMapping("/applicants")
    public ResponseEntity<List<FriendDto>> friendApplyList(@AuthenticationPrincipal PrincipalDetails principal) {
        User user = userService.getUserByEmail(principal.getEmail());
        return ResponseEntity.ok(friendService.applicants(user.getId()).stream()
            .map(friend -> FriendDto.of(friend.getId(), friend.getUser()))
            .collect(Collectors.toList())
        );
    }

    // 유저가 친구 요청한 목록 조회
    @GetMapping("/requests")
    public ResponseEntity<List<FriendDto>> friendRequestList(@AuthenticationPrincipal PrincipalDetails principal) {
        User user = userService.getUserByEmail(principal.getEmail());
        return ResponseEntity.ok(friendService.requests(user.getId()).stream()
            .map(friend -> FriendDto.of(friend.getId(), friend.getFriendUser()))
            .collect(Collectors.toList())
        );
    }

    // 친구 요청
    @PostMapping("/users/{targetUserId}")
    public void register(
        @AuthenticationPrincipal PrincipalDetails principal, @PathVariable("targetUserId") Long targetUserId
    ) {
        User user = userService.getUserByEmail(principal.getEmail());
        User targetUser = userService.findById(targetUserId);
        Long userId = user.getId();
        if (Objects.equals(userId, targetUserId)) {
            throw new CustomException(BAD_REQUEST);
        }
        Friend existFriend = friendService.findByUserIdAndFriendUserId(userId, targetUserId);
        if (existFriend != null) {
            ApplyStatus status = existFriend.getStatus();
            if (status == ApplyStatus.PENDING) {
                throw new CustomException(ALREADY_REQUESTED_FRIEND);
            }
            if (status == ApplyStatus.ACCEPT) {
                throw new CustomException(ALREADY_REGISTERED_FRIEND);
            }
        }
        // 차단 유저인지 확인
        if (blockService.existsByUserIdAndBlockUserId(userId, targetUserId)) {
            throw new CustomException(ALREADY_REGISTERED_BLOCKED);
        }
        // 상대가 유저를 차단한 경우
        if (blockService.existsByUserIdAndBlockUserId(targetUserId, userId)) {
            throw new CustomException(NOT_FOUND_USER);
        }

        friendService.requestAndAlarm(user, targetUser);
    }

    // 친구 요청 수락
    @PutMapping("/{friendId}/accept")
    public void accept(@AuthenticationPrincipal PrincipalDetails principal, @PathVariable("friendId") Long friendId) {
        User user = userService.getUserByEmail(principal.getEmail());
        Friend friendRequest = friendService.findById(friendId);
        if (!Objects.equals(user.getId(), friendRequest.getFriendUser().getId())) {
            throw new CustomException(BAD_REQUEST);
        }

        friendService.acceptAndAlarm(user, friendRequest);
    }

    // 친구 요청 거절
    @DeleteMapping("/{friendId}/reject")
    public void reject(@AuthenticationPrincipal PrincipalDetails principal, @PathVariable("friendId") Long friendId) {
        User user = userService.getUserByEmail(principal.getEmail());
        Friend friendRequest = friendService.findById(friendId);
        if (!Objects.equals(user.getId(), friendRequest.getFriendUser().getId())) {
            throw new CustomException(BAD_REQUEST);
        }

        friendService.rejectAndAlarm(user, friendRequest);
    }

    // 친구 삭제, 친구 요청 취소
    @DeleteMapping("/{friendId}")
    public void delete(@AuthenticationPrincipal PrincipalDetails principal, @PathVariable("friendId") Long friendId) {
        User user = userService.getUserByEmail(principal.getEmail());
        Friend friend = friendService.findById(friendId);
        if (!Objects.equals(user.getId(), friend.getUser().getId())) {
            throw new CustomException(BAD_REQUEST);
        }
        friendService.delete(friend);
    }
}
