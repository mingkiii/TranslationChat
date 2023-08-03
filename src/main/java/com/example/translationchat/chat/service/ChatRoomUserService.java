package com.example.translationchat.chat.service;

import static com.example.translationchat.common.exception.ErrorCode.NOT_FOUND_USER;
import static com.example.translationchat.common.exception.ErrorCode.OFFLINE_USER;
import static com.example.translationchat.common.exception.ErrorCode.USER_IS_BLOCKED;

import com.example.translationchat.client.domain.form.NotificationForm;
import com.example.translationchat.client.domain.model.Favorite;
import com.example.translationchat.client.domain.model.User;
import com.example.translationchat.client.domain.repository.FavoriteRepository;
import com.example.translationchat.client.domain.repository.UserRepository;
import com.example.translationchat.client.domain.type.ActiveStatus;
import com.example.translationchat.client.domain.type.ContentType;
import com.example.translationchat.client.service.NotificationService;
import com.example.translationchat.common.exception.CustomException;
import com.example.translationchat.common.security.principal.PrincipalDetails;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRoomUserService {

    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final NotificationService notificationService;

    // 대화 요청
    public void request(Authentication authentication, Long receiverUserId) {
        User user = getUser(authentication);
        User receiver = userRepository.findById(receiverUserId)
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

        // 유저가 차단한 유저인지 확인 -> 차단한 경우 요청되지 않음
        Optional<Favorite> optionalFavoriteOfUser =
            favoriteRepository.findByUserAndFavoriteUser(user, receiver);
        if (optionalFavoriteOfUser.isPresent()) {
            Favorite favoriteUser = optionalFavoriteOfUser.get();
            if (favoriteUser.isBlocked()) {
                throw new CustomException(USER_IS_BLOCKED);
            }
        }

        // 상대가 유저를 차단한 상태인지 확인
        // 차단 당한 상태일 경우 - 오프라인 상태 예외발생으로 요청 되지 않도록 함.
        Optional<Favorite> optionalFavoriteOfReceiver =
            favoriteRepository.findByUserAndFavoriteUser(receiver, user);
        if (optionalFavoriteOfReceiver.isPresent()) {
            Favorite favoriteReceiver = optionalFavoriteOfReceiver.get();
            if (favoriteReceiver.isBlocked()) {
                throw new CustomException(OFFLINE_USER);
            }
        }

        if (ActiveStatus.ONLINE.equals(receiver.getStatus())) {
            // 요청받는 유저에게 알림 생성
            String message = String.format("%s 님이 %s 님에게 %s",
                user.getName(), receiver.getName(),
                ContentType.REQUEST_CHAT.getDisplayName());

            notificationService.create
                (NotificationForm.builder()
                        .user(receiver)
                        .args(user.getId())
                        .contentType(ContentType.REQUEST_CHAT)
                        .build(), message);
        } else {
            throw new CustomException(OFFLINE_USER);
        }
    }

    private User getUser(Authentication authentication) {
        PrincipalDetails details = (PrincipalDetails) authentication.getPrincipal();
        return details.getUser();
    }
}
