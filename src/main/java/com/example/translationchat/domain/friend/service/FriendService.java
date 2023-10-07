package com.example.translationchat.domain.friend.service;

import static com.example.translationchat.common.exception.ErrorCode.NOT_FOUND_FAVORITE;

import com.example.translationchat.common.exception.CustomException;
import com.example.translationchat.domain.friend.entity.Friend;
import com.example.translationchat.domain.friend.repository.FriendRepository;
import com.example.translationchat.domain.notification.form.NotificationForm;
import com.example.translationchat.domain.type.ApplyStatus;
import com.example.translationchat.domain.type.ContentType;
import com.example.translationchat.domain.user.entity.User;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final ApplicationEventPublisher eventPublisher;
    private final FriendRepository friendRepository;

    public Friend findById(Long friendUserId) {
        return friendRepository.findById(friendUserId)
            .orElseThrow(() -> new CustomException(NOT_FOUND_FAVORITE));
    }

    public Friend findByUserIdAndFriendUserId(Long userId, Long friendUserId) {
        return friendRepository.findByUserIdAndFriendUserId(userId, friendUserId)
            .orElse(null);
    }

    // 친구 요청
    @Transactional
    public void requestAndAlarm(User user, User targetUser) {
        friendRepository.save(Friend.builder()
                .user(user)
                .friendUser(targetUser)
                .status(ApplyStatus.PENDING)
                .build()
        );
        alarmEventPub(targetUser, user, ContentType.REQUEST_FRIEND);
    }

    // 친구 요청 수락
    @Transactional
    public void acceptAndAlarm(User user, Friend friendRequest) {
        User requester = friendRequest.getUser();
        friendRequest.setStatus(ApplyStatus.ACCEPT);
        friendRepository.save(friendRequest);

        friendRepository.save(Friend.builder()
            .user(user)
            .friendUser(requester)
            .status(ApplyStatus.ACCEPT)
            .build()
        );

        alarmEventPub(requester, user, ContentType.ACCEPT_FRIEND);
    }

    // 친구 요청 거절
    @Transactional
    public void rejectAndAlarm(User user, Friend friendRequest) {
        User requester = friendRequest.getUser();
        friendRepository.delete(friendRequest);

        alarmEventPub(requester, user, ContentType.REJECT_FRIEND);
    }

    // 친구 삭제
    @Transactional
    public void delete(Friend friend) {
        friendRepository.delete(friend);
    }

    // 유저가 받은 친구 신청 목록
    public List<Friend> applicants(Long userId) {
        return friendRepository.findAllByFriendUserId(userId).stream()
            .filter(friend -> friend.getStatus() == ApplyStatus.PENDING)
            .collect(Collectors.toList());
    }

    // 유저가 친구 신청한 목록
    public List<Friend> requests(Long userId) {
        return friendRepository.findAllByUserId(userId).stream()
            .filter(friend -> friend.getStatus() == ApplyStatus.PENDING)
            .collect(Collectors.toList());
    }

    public List<Friend> friends(Long userId) {
        return friendRepository.findAllByUserId(userId).stream()
            .filter(friend -> friend.getStatus() == ApplyStatus.ACCEPT)
            .collect(Collectors.toList());
    }

    private void alarmEventPub(User receiver, User sendUser, ContentType content) {
        eventPublisher.publishEvent(NotificationForm.of(receiver, sendUser, content));
    }
}
