package com.example.translationchat.chat.service;

import static com.example.translationchat.common.exception.ErrorCode.ALREADY_RANDOM_CHAT_ROOM;
import static com.example.translationchat.common.exception.ErrorCode.LOCK_FAILED;
import static com.example.translationchat.common.exception.ErrorCode.NOT_EXIST_CLIENT;
import static com.example.translationchat.common.exception.ErrorCode.NOT_INVALID_ROOM;
import static com.example.translationchat.common.exception.ErrorCode.RANDOM_CHAT_UNAVAILABLE_STATUS;

import com.example.translationchat.chat.domain.model.RandomChatRoom;
import com.example.translationchat.chat.domain.repository.RandomChatRoomRepository;
import com.example.translationchat.chat.domain.request.RandomChatMessageRequest;
import com.example.translationchat.client.domain.model.User;
import com.example.translationchat.client.domain.type.ActiveStatus;
import com.example.translationchat.client.domain.type.Language;
import com.example.translationchat.client.service.NotificationService;
import com.example.translationchat.client.service.ReportService;
import com.example.translationchat.common.exception.CustomException;
import com.example.translationchat.common.papago.PapagoService;
import com.example.translationchat.common.redis.util.RedisLockUtil;
import com.example.translationchat.common.security.principal.PrincipalDetails;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RandomChatService {

    private final SimpMessagingTemplate messagingTemplate;
    private final RandomChatRoomRepository randomChatRoomRepository;
    private final NotificationService notificationService;
    private final PapagoService papagoService;
    private final ReportService reportService;

    private final RedisLockUtil redisLockUtil;
    private final String LOCK_KEY = "QUEUE_LOCK";
    private final Queue<User> queue = new LinkedList<>();

    @Transactional
    public void joinRandomChat(Authentication authentication) {
        User user = getUser(authentication);

        // 랜덤 채팅 이용 불가 상태 체크
        if (!user.isRandomApproval()) {
            // 이용 정지 기간 지났는지 확인
            if (!reportService.isReportDateOlderThanAWeek(user)) {
                throw new CustomException(RANDOM_CHAT_UNAVAILABLE_STATUS);
            }
        }

        // 이미 참여한 방이 있는지 확인
        if (randomChatRoomRepository.existsByJoinUser1OrJoinUser2(user, user)) {
            throw new CustomException(ALREADY_RANDOM_CHAT_ROOM);
        }

        try {
            boolean locked = redisLockUtil.getLock(LOCK_KEY, 5);
            if (locked) {
                queue.add(user);
            } else {
                throw new CustomException(LOCK_FAILED);
            }
        } finally {
            redisLockUtil.unLock(LOCK_KEY);
        }

        tryMatchAndCreateChatRoom();
    }

    @Transactional
    public void tryMatchAndCreateChatRoom() {
        List<User> matchedUsers = new ArrayList<>();

        // 매칭할 사용자들 선택
        // 큐가 비었다면 다른 유저에 의해 매칭된 경우
        while (!queue.isEmpty() && matchedUsers.size() < 2) {
            User user;
            try {
                boolean locked = redisLockUtil.getLock(LOCK_KEY, 5);
                if (locked) {
                    user = queue.poll();
                } else {
                    throw new CustomException(LOCK_FAILED);
                }
            } finally {
                redisLockUtil.unLock(LOCK_KEY);
            }
            // 사용자가 온라인 상태인 경우에만 매칭 대상에 추가
            if (Objects.requireNonNull(user).getStatus() == ActiveStatus.ONLINE) {
                matchedUsers.add(user);
            }
        }

        if (matchedUsers.size() == 2) {
            createChatRoom(matchedUsers.get(0), matchedUsers.get(1));
        } else if (matchedUsers.size() == 1) {
            // 매칭 실패한 경우 큐에 다시 넣음
            try {
                boolean locked = redisLockUtil.getLock(LOCK_KEY, 5);
                if (locked) {
                    queue.add(matchedUsers.get(0));
                } else {
                    throw new CustomException(LOCK_FAILED);
                }
            } finally {
                redisLockUtil.unLock(LOCK_KEY);
            }
        }
    }

    @Transactional
    public void createChatRoom(User user1, User user2) {
        randomChatRoomRepository.save(RandomChatRoom.builder()
            .joinUser1(user1)
            .joinUser2(user2)
            .createdTime(Instant.now())
            .build());
    }

    // 누구라도 나가면 방 삭제
    @Transactional
    public void outRoom(Authentication authentication, Long roomId) {
        User user = getUser(authentication);

        RandomChatRoom room = randomChatRoomRepository.findById(roomId)
            .orElseThrow(() -> new CustomException(NOT_INVALID_ROOM));

        User otherUser = (room.getJoinUser1() == user) ? room.getJoinUser2() : room.getJoinUser1();

        if (otherUser != null) {
            notificationService.sendNotificationMessage(otherUser.getId(), "상대방이 방을 나갔습니다.");
        }

        randomChatRoomRepository.delete(room);
    }

    public void sendMessage(Authentication authentication, Long roomId, RandomChatMessageRequest messageRequest) {
        User user = getUser(authentication);
        Language language = user.getLanguage();
        RandomChatRoom room = randomChatRoomRepository.findById(roomId)
            .orElseThrow(() -> new CustomException(NOT_INVALID_ROOM));

        User otherUser = (room.getJoinUser1() == user) ? room.getJoinUser2() : room.getJoinUser1();
        if (otherUser == null) {
            throw new CustomException(NOT_EXIST_CLIENT);
        }
        Language transLanguage = otherUser.getLanguage();
        String message = messageRequest.getContent();
        String transSentence = translateIfNeeded(message, language, transLanguage);
        String formattedMessage = generateFormattedMessage(message, transSentence);
        messagingTemplate.convertAndSend("/sub/random/chat/" + room.getId(), formattedMessage);
    }

    private User getUser(Authentication authentication) {
        PrincipalDetails details = (PrincipalDetails) authentication.getPrincipal();
        return details.getUser();
    }

    private String translateIfNeeded(String message, Language fromLanguage, Language toLanguage) {
        if (fromLanguage != toLanguage) {
            return papagoService.getTransSentence(message, fromLanguage, toLanguage);
        }
        return "";
    }
    private String generateFormattedMessage(String originalMessage, String translatedMessage) {
        if (translatedMessage.isEmpty()) {
            return originalMessage;
        }
        return originalMessage + "\n" + translatedMessage;
    }
}
