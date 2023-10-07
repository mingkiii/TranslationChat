package com.example.translationchat.domain.chat.controller;

import static com.example.translationchat.common.exception.ErrorCode.BAD_REQUEST;
import static com.example.translationchat.common.exception.ErrorCode.RANDOM_CHAT_UNAVAILABLE_STATUS;

import com.example.translationchat.common.exception.CustomException;
import com.example.translationchat.common.security.principal.PrincipalDetails;
import com.example.translationchat.domain.chat.dto.RandomChatRoomDto;
import com.example.translationchat.domain.chat.entity.RandomChatRoom;
import com.example.translationchat.domain.chat.service.RandomChatMessageService;
import com.example.translationchat.domain.chat.service.RandomChatRoomService;
import com.example.translationchat.domain.report.service.ReportService;
import com.example.translationchat.domain.user.entity.User;
import com.example.translationchat.domain.user.service.UserService;
import java.util.Objects;
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
@RequestMapping("/api/chat/random/rooms")
@RequiredArgsConstructor
public class RandomChatRoomController {

    private final RandomChatMessageService chatMessageService;
    private final RandomChatRoomService randomChatRoomService;
    private final UserService userService;
    private final ReportService reportService;

    @GetMapping
    public ResponseEntity<RandomChatRoomDto> findJoinRoom(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        User user = userService.getUserByEmail(principalDetails.getEmail());
        RandomChatRoom chatRoom = randomChatRoomService.findByUserId(user.getId());
        return ResponseEntity.ok(RandomChatRoomDto.from(chatRoom));
    }

    // 랜덤 채팅 시작 -> 매칭 후 방 생성
    @PostMapping("/join")
    public void joinRoom(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        User user = userService.getUserByEmail(principalDetails.getEmail());
        // 랜덤 채팅 이용 불가 상태 체크
        if (!user.isRandomApproval()) {
            // 이용 정지 기간 지났는지 확인
            if (!reportService.isReportDateOlderThanAWeek(user)) {
                throw new CustomException(RANDOM_CHAT_UNAVAILABLE_STATUS);
            }
            // (7일)정지 기간 지난 경우 유저 랜덤 채팅 서비스 승인으로 변경
            userService.updateRandomApproval(user);
        }
        randomChatRoomService.joinRandomChat(user);
    }

    // 방 나가기 -> 방 삭제
    @DeleteMapping("/{roomId}")
    public void outRoom(
        @AuthenticationPrincipal PrincipalDetails principalDetails,
        @PathVariable("roomId") Long roomId
    ) {
        User user = userService.getUserByEmail(principalDetails.getEmail());
        RandomChatRoom randomChatRoom = randomChatRoomService.findByRoomId(roomId);
        Long userId = user.getId();
        if (!Objects.equals(randomChatRoom.getJoinUser1().getId(), userId)
            && !Objects.equals(randomChatRoom.getJoinUser2().getId(), userId)) {
            throw new CustomException(BAD_REQUEST);
        }
        chatMessageService.deleteOfChatRoomId(roomId);
        randomChatRoomService.exit(randomChatRoom);
    }
}
