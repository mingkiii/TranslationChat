package com.example.translationchat.domain.chat.service;

import static com.example.translationchat.common.exception.ErrorCode.ALREADY_RANDOM_CHAT_ROOM;
import static com.example.translationchat.common.exception.ErrorCode.LOCK_FAILED;
import static com.example.translationchat.common.exception.ErrorCode.NOT_INVALID_ROOM;

import com.example.translationchat.common.exception.CustomException;
import com.example.translationchat.common.redis.RedisService;
import com.example.translationchat.domain.chat.entity.RandomChatRoom;
import com.example.translationchat.domain.chat.repository.RandomChatRoomRepository;
import com.example.translationchat.domain.type.ActiveStatus;
import com.example.translationchat.domain.user.entity.User;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RandomChatRoomService {

    private final RandomChatRoomRepository randomChatRoomRepository;
    private final RedisService redisService;

    private final String LOCK_KEY = "QUEUE_LOCK";
    private final String QUEUE_KEY = "random_chat_queue";

    @Transactional
    public void joinRandomChat(User user) {
        Long userId = user.getId();
        // 이미 참여한 방이 있는지 확인
        if (randomChatRoomRepository.existsByJoinUser1IdOrJoinUser2Id(userId, userId)) {
            throw new CustomException(ALREADY_RANDOM_CHAT_ROOM);
        }

        try {
            boolean locked = redisService.getLock(LOCK_KEY, 5);
            if (locked) {
                // Redis 를 이용하여 큐에 사용자 추가
                redisService.push(QUEUE_KEY, user);
            } else {
                throw new CustomException(LOCK_FAILED);
            }
        } finally {
            redisService.unLock(LOCK_KEY);
        }

        tryMatchAndCreateChatRoom();
    }

    @Transactional
    public void tryMatchAndCreateChatRoom() {
        List<User> matchedUsers = new ArrayList<>();

        // 매칭할 사용자들 선택
        while (matchedUsers.size() < 2) {
            try {
                boolean locked = redisService.getLock(LOCK_KEY, 5);
                if (locked) {
                    // Redis 를 이용하여 큐에서 사용자 제거
                    User user = redisService.pop(QUEUE_KEY);
                    if (user == null) {
                        break; // 큐가 비었다면 종료. 다른 유저에 의해 매칭된 경우
                    }
                    // 사용자가 온라인 상태, 랜덤 이용 가능한 경우에만 매칭 대상에 추가
                    if (user.getStatus() == ActiveStatus.ONLINE && user.isRandomApproval()) {
                        matchedUsers.add(user);
                    }
                } else {
                    throw new CustomException(LOCK_FAILED);
                }
            } finally {
                redisService.unLock(LOCK_KEY);
            }
        }

        if (matchedUsers.size() == 2) {
            createChatRoom(matchedUsers.get(0), matchedUsers.get(1));
        } else if (matchedUsers.size() == 1) {
            // 매칭 실패한 경우 큐에 다시 넣음
            try {
                boolean locked = redisService.getLock(LOCK_KEY, 5);
                if (locked) {
                    redisService.push(QUEUE_KEY, matchedUsers.get(0));
                } else {
                    throw new CustomException(LOCK_FAILED);
                }
            } finally {
                redisService.unLock(LOCK_KEY);
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

    public RandomChatRoom findByUserId(Long userId) {
        return randomChatRoomRepository.findByJoinUser1IdOrJoinUser2Id(userId, userId).orElse(null);
    }

    public RandomChatRoom findByRoomId(Long roomId) {
        return randomChatRoomRepository.findById(roomId)
            .orElseThrow(() -> new CustomException(NOT_INVALID_ROOM));
    }

    // 누구라도 나가면 방 삭제
    @Transactional
    public void exit(RandomChatRoom room) {
        randomChatRoomRepository.delete(room);
    }
}
