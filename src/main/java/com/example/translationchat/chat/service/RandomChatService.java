package com.example.translationchat.chat.service;

import static com.example.translationchat.common.exception.ErrorCode.ALREADY_RANDOM_CHAT_ROOM;
import static com.example.translationchat.common.exception.ErrorCode.LOCK_FAILED;
import static com.example.translationchat.common.exception.ErrorCode.NOT_FOUND_RANDOM_CHAT_ROOM;

import com.example.translationchat.chat.domain.model.RandomChatRoom;
import com.example.translationchat.chat.domain.repository.RandomChatRoomRepository;
import com.example.translationchat.client.domain.model.User;
import com.example.translationchat.common.exception.CustomException;
import com.example.translationchat.common.redis.util.RedisLockUtil;
import com.example.translationchat.common.security.principal.PrincipalDetails;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RandomChatService {

    private final SimpMessagingTemplate messagingTemplate;
    private final RandomChatRoomRepository randomChatRoomRepository;
    private final RedisLockUtil redisLockUtil;

    private final String KEY = "RANDOM_CHAT_ROOM";

    @Transactional
    public String createRoom(Authentication authentication) {
        User user = getUser(authentication);
        // 락 : 방 탐색 키랑 동일
        try {
            boolean roomLocked = redisLockUtil.getLock(KEY, 5);
            if (roomLocked) {
                randomChatRoomRepository.save(RandomChatRoom.builder()
                    .createUser(user)
                    .createdTime(Instant.now())
                    .build());
                return "새로운 랜덤 채팅방을 생성하였습니다.";
            } else {
                throw new CustomException(LOCK_FAILED);
            }
        } catch (Exception e) {
            redisLockUtil.unLock(KEY);
            throw e;
        } finally {
            redisLockUtil.unLock(KEY);
        }
    }

    @Transactional
    public void joinRandomChat(Authentication authentication) {
        User user = getUser(authentication);

        // 이미 참여한 방이 있는지 확인
        if (randomChatRoomRepository.existsByCreateUserOrJoinUser(user, user)) {
            throw new CustomException(ALREADY_RANDOM_CHAT_ROOM);
        }
        // 참여 가능한 방이 있는지 탐색( 락 : 방 생성 키랑 동일)
        try {
            boolean roomLocked = redisLockUtil.getLock(KEY, 5);
            if (roomLocked) {
                RandomChatRoom randomChatRoom = randomChatRoomRepository.findFirstByJoinUserIsNull()
                    .orElseThrow(() -> new CustomException(NOT_FOUND_RANDOM_CHAT_ROOM));
                randomChatRoom.setJoinUser(user);
                randomChatRoomRepository.save(randomChatRoom);
            } else {
                throw new CustomException(LOCK_FAILED);
            }
        } catch (Exception e) {
            redisLockUtil.unLock(KEY);
            throw e;
        } finally {
            redisLockUtil.unLock(KEY);
        }
    }

    @Scheduled(fixedDelay = 60000) // 1분마다 실행
    public void checkEmptyRooms() {
        Instant fiveMinutesAgo = Instant.now().minus(5, ChronoUnit.MINUTES);
        List<RandomChatRoom> emptyRooms = randomChatRoomRepository.findByJoinUserIsNullAndCreatedTimeBefore(fiveMinutesAgo);

        if (emptyRooms != null && !emptyRooms.isEmpty()) {
            for (RandomChatRoom room : emptyRooms) {
                // 방 삭제 로직
                randomChatRoomRepository.delete(room);

                // 클라이언트에게 메시지 전송
                messagingTemplate.convertAndSend("/sub/random/room/create", "5분동안 참여자가 없어 방이 삭제되었습니다.");
            }
        }

    }

    private User getUser(Authentication authentication) {
        PrincipalDetails details = (PrincipalDetails) authentication.getPrincipal();
        return details.getUser();
    }
}
