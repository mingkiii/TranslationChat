package com.example.translationchat.client.service;

import static com.example.translationchat.common.exception.ErrorCode.ALREADY_OPPONENT_REQUEST;
import static com.example.translationchat.common.exception.ErrorCode.ALREADY_REGISTERED_FRIEND;
import static com.example.translationchat.common.exception.ErrorCode.ALREADY_REQUEST_FRIENDSHIP;
import static com.example.translationchat.common.exception.ErrorCode.CAN_NOT_FRIEND_YOURSELF;
import static com.example.translationchat.common.exception.ErrorCode.FRIENDSHIP_STATUS_IS_BLOCKED;
import static com.example.translationchat.common.exception.ErrorCode.FRIENDSHIP_STATUS_IS_NOT_BLOCKED;
import static com.example.translationchat.common.exception.ErrorCode.NOT_FOUND_FRIENDSHIP;
import static com.example.translationchat.common.exception.ErrorCode.NOT_FOUND_USER;
import static com.example.translationchat.common.exception.ErrorCode.NOT_REGISTERED_FRIEND;

import com.example.translationchat.client.domain.dto.FriendInfoDto;
import com.example.translationchat.client.domain.form.NotificationForm;
import com.example.translationchat.client.domain.model.Friendship;
import com.example.translationchat.client.domain.model.User;
import com.example.translationchat.client.domain.repository.FriendshipRepository;
import com.example.translationchat.client.domain.repository.UserRepository;
import com.example.translationchat.client.domain.type.ContentType;
import com.example.translationchat.client.domain.type.FriendshipStatus;
import com.example.translationchat.common.exception.CustomException;
import com.example.translationchat.common.security.principal.PrincipalDetails;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final NotificationService notificationService;

    public String requestFriend(Authentication authentication, String friendName) {
        // 유저 : A, 친구 : B
        User user = getUser(authentication);
        if (user.getName().equals(friendName)) {
            throw new CustomException(CAN_NOT_FRIEND_YOURSELF);
        }
        User friend = userRepository.findByName(friendName)
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER));
        // 유저 A의 친구기록을 가져옵니다.
        Optional<Friendship> optionalUserFriendship =
            friendshipRepository.findByUserAndFriend(user, friend);
        // 유저 B의 친구기록을 가져옵니다.
        Optional<Friendship> optionalFriendFriendship =
            friendshipRepository.findByUserAndFriend(friend, user);

        if (optionalFriendFriendship.isPresent()) {
            Friendship friendFriendship = optionalFriendFriendship.get();
            FriendshipStatus status = friendFriendship.getFriendshipStatus();
            // 유저 B가 A에게 이미 친구 요청 중인 경우
            if (status == FriendshipStatus.PENDING) {
                throw new CustomException(ALREADY_OPPONENT_REQUEST);
            }
        }

        if (optionalUserFriendship.isPresent()) {
            Friendship userFriendship = optionalUserFriendship.get();
            FriendshipStatus status = userFriendship.getFriendshipStatus();
            switch (status) {
                case ACCEPTED:
                    throw new CustomException(ALREADY_REGISTERED_FRIEND);
                case PENDING:
                    // A가 B에게 친구 요청한 상태인 경우
                    LocalDateTime requestTime = userFriendship.getRequestTime();
                    if (requestTime.isBefore(LocalDateTime.now().minusMonths(1))) {
                        // 요청날짜가 한달이 지났으면 다시 요청
                        // 친구 요청 시간 초기화
                        userFriendship.setRequestTime(LocalDateTime.now());
                        friendshipRepository.save(userFriendship);
                    } else {
                        // 요청날짜가 한달 미만이면 이미 요청 중인 상태로 유지
                        throw new CustomException(ALREADY_REQUEST_FRIENDSHIP);
                    }
                    break;
                case BLOCKED:
                    throw new CustomException(FRIENDSHIP_STATUS_IS_BLOCKED);
            }
        } else {
            // A의 '친구레파지토리'에 요청상태 저장
            friendshipRepository.save(Friendship.builder()
                .user(user)
                .friend(friend)
                .requestTime(LocalDateTime.now())
                .friendshipStatus(FriendshipStatus.PENDING)
                .build());
        }

        NotificationForm form = NotificationForm.builder()
            .user(friend)
            .args(user.getName())
            .contentType(ContentType.FRIEND_REQUEST)
            .build();
        // 상대방에게 친구요청 알림 생성
        notificationService.create(form);

        return String.format("%s 님에게 %s",
            friendName, ContentType.FRIEND_REQUEST.getDisplayName());
    }

    // 친구 요청 수락
    public String acceptFriendship(
        Authentication authentication, Long notificationId, String requesterName
    ) {
        User user = getUser(authentication);
        User requester = userRepository.findByName(requesterName)
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

        // 요청 받은 유저의 '친구 레파지토리'에 요청자가 있는지 확인
        Optional<Friendship> optionalUserFriendship =
            friendshipRepository.findByUserAndFriend(user, requester);
        Friendship requesterFriendship =
            friendshipRepository.findByUserAndFriend(requester, user)
            .orElseThrow(() -> new CustomException(NOT_FOUND_FRIENDSHIP));

        if (optionalUserFriendship.isPresent()) {
            Friendship userFriendship = optionalUserFriendship.get();
            FriendshipStatus status = userFriendship.getFriendshipStatus();

            switch (status) {
                case ACCEPTED:
                    throw new CustomException(ALREADY_REGISTERED_FRIEND);

                case BLOCKED:
                    throw new CustomException(FRIENDSHIP_STATUS_IS_BLOCKED);
            }
        } else {
            // 서로 친구 상태로 저장
            friendshipRepository.save(Friendship.builder()
                .user(user)
                .friend(requester)
                .acceptTime(LocalDateTime.now())
                .friendshipStatus(FriendshipStatus.ACCEPTED)
                .build());

            requesterFriendship.setFriendshipStatus(FriendshipStatus.ACCEPTED);
            requesterFriendship.setAcceptTime(LocalDateTime.now());
            friendshipRepository.save(requesterFriendship);
        }
        NotificationForm form = NotificationForm.builder()
            .user(requester)
            .args(user.getName())
            .contentType(ContentType.SUCCESS_FRIENDSHIP)
            .build();
        notificationService.create(form);
        // 요청에 대한 알림을 읽을 걸로 간주하여 해당 알림 삭제합니다.
        notificationService.delete(notificationId);

        return String.format("%s 님과 %s",
            requesterName, ContentType.SUCCESS_FRIENDSHIP.getDisplayName());
    }

    // 친구 요청 거절
    public String refuseFriendship(
        Authentication authentication, Long notificationId, String requesterName
    ) {
        User user = getUser(authentication);
        User requester = userRepository.findByName(requesterName)
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

        // 요청 받은 유저의 '친구 레파지토리'에 요청자가 있는지 확인
        Optional<Friendship> optionalUserFriendship =
            friendshipRepository.findByUserAndFriend(user, requester);
        Friendship requesterFriendship =
            friendshipRepository.findByUserAndFriend(requester, user)
                .orElseThrow(() -> new CustomException(NOT_FOUND_FRIENDSHIP));

        if (optionalUserFriendship.isPresent()) {
            Friendship userFriendship = optionalUserFriendship.get();
            FriendshipStatus status = userFriendship.getFriendshipStatus();
            if (status == FriendshipStatus.ACCEPTED) {
                throw new CustomException(ALREADY_REGISTERED_FRIEND);
            }
            // 친구를 차단한 상태인 경우도 거절한 것으로 처리됩니다.
        }
        // 거절로 요청자의 요청받은 유저와의 친구기록을 삭제합니다.
        friendshipRepository.delete(requesterFriendship);
        NotificationForm form = NotificationForm.builder()
            .user(requester)
            .args(user.getName())
            .contentType(ContentType.REFUSE_FRIEND_REQUEST)
            .build();

        notificationService.create(form);

        // 받은 요청에 대한 알림을 읽을 걸로 간주하여 해당 알림 삭제합니다.
        notificationService.delete(notificationId);

        return String.format("%s %s",
            requesterName, ContentType.REFUSE_FRIEND_REQUEST.getDisplayName());
    }

    // 유저 차단
    public String block(Authentication authentication, String friendName) {
        User user = getUser(authentication);
        User friend = userRepository.findByName(friendName)
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

        // 유저의 '친구 레파지토리'에 친구가 있는지 확인
        Optional<Friendship> optionalUserFriendship =
            friendshipRepository.findByUserAndFriend(user, friend);
        if (optionalUserFriendship.isPresent()) {
            Friendship userFriendship = optionalUserFriendship.get();
            userFriendship.setFriendshipStatus(FriendshipStatus.BLOCKED);
            friendshipRepository.save(userFriendship);
        } else {
            friendshipRepository.save(Friendship.builder()
                .user(user)
                .friend(friend)
                .friendshipStatus(FriendshipStatus.BLOCKED)
                .build()
            );
        }
        return friendName + " 님을 차단했습니다.";
    }

    // 차단 해제 -> 친구 관계도 아닌 상태가 됩니다.
    public String unBlock(Authentication authentication, String friendName) {
        User user = getUser(authentication);
        User friend = userRepository.findByName(friendName)
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

        // 유저의 '친구 레파지토리'에 친구가 있는지 확인
        Optional<Friendship> optionalUserFriendship =
            friendshipRepository.findByUserAndFriend(user, friend);
        if (optionalUserFriendship.isPresent()) {
            Friendship userFriendship = optionalUserFriendship.get();
            if (userFriendship.getFriendshipStatus()
                == FriendshipStatus.BLOCKED) {
                friendshipRepository.delete(userFriendship);
            } else {
                throw new CustomException(FRIENDSHIP_STATUS_IS_NOT_BLOCKED);
            }
        } else {
            throw new CustomException(FRIENDSHIP_STATUS_IS_NOT_BLOCKED);
        }
        return friendName + " 님을 차단 해제 했습니다.";
    }

    // 친구 관계 상태에 대한 친구목록 조회 (친구관계인 친구목록, 친구요청한 친구목록, 차단한 친구목록)
    public Set<FriendInfoDto> getFriends(
        Authentication authentication, FriendshipStatus status)
    {
        User user = getUser(authentication);
        return friendshipRepository.findByUserAndFriendshipStatus(user, status)
            .stream()
            .map(Friendship::getFriend)
            .map(FriendInfoDto::from)
            .collect(Collectors.toSet());
    }

    // 친구 삭제
    public void delete(Authentication authentication, String friendName) {
        User user = getUser(authentication);
        User friend = userRepository.findByName(friendName)
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

        Friendship friendship = friendshipRepository.findByUserAndFriend(user, friend)
            .orElseThrow(() -> new CustomException(NOT_FOUND_FRIENDSHIP));
        if (friendship.getFriendshipStatus() == FriendshipStatus.ACCEPTED) {
            friendshipRepository.delete(friendship);
        } else {
            throw new CustomException(NOT_REGISTERED_FRIEND);
        }
    }

    private User getUser(Authentication authentication) {
        PrincipalDetails details = (PrincipalDetails) authentication.getPrincipal();
        return details.getUser();
    }
}
