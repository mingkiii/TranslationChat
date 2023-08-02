package com.example.translationchat.client.service;

import static com.example.translationchat.common.exception.ErrorCode.ALREADY_REGISTERED_FAVORITE;
import static com.example.translationchat.common.exception.ErrorCode.CAN_NOT_FAVORITE_YOURSELF;
import static com.example.translationchat.common.exception.ErrorCode.NOT_FOUND_USER;
import static com.example.translationchat.common.exception.ErrorCode.NOT_REGISTERED_FAVORITE;
import static com.example.translationchat.common.exception.ErrorCode.USER_IS_BLOCKED;
import static com.example.translationchat.common.exception.ErrorCode.USER_IS_NOT_BLOCKED;

import com.example.translationchat.client.domain.dto.UserInfoDto;
import com.example.translationchat.client.domain.model.Favorite;
import com.example.translationchat.client.domain.model.User;
import com.example.translationchat.client.domain.repository.FavoriteRepository;
import com.example.translationchat.client.domain.repository.UserRepository;
import com.example.translationchat.common.exception.CustomException;
import com.example.translationchat.common.security.principal.PrincipalDetails;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;

    // 즐겨찾기 등록
    @Transactional
    public String register(Authentication authentication, Long userId) {
        User user = getUser(authentication);
        // 유저 자신을 즐겨찾기에 추가하는 경우
        if (user.getId().equals(userId)) {
            throw new CustomException(CAN_NOT_FAVORITE_YOURSELF);
        }
        User favoriteUser = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

        // 유저의 '즐겨찾기 레파지토리'에 관심유저가 있는지 확인
        Optional<Favorite> optionalFavoriteOfUser =
            favoriteRepository.findByUserAndFavoriteUser(user, favoriteUser);
        if (optionalFavoriteOfUser.isPresent()) {
            Favorite favoriteOfUser = optionalFavoriteOfUser.get();
            // 차단한 경우
            if (favoriteOfUser.isBlocked()) {
                throw new CustomException(USER_IS_BLOCKED);
            }
            // 이미 등록된 경우
            throw new CustomException(ALREADY_REGISTERED_FAVORITE);
        } else {
            favoriteRepository.save(Favorite.builder()
                .user(user)
                .favoriteUser(favoriteUser)
                .blocked(false)
                .build()
            );
        }
        return favoriteUser.getName() + " 님을 즐겨찾기에 추가했습니다.";
    }

    // 즐겨찾기 삭제
    @Transactional
    public void delete(Authentication authentication, Long userId) {
        User user = getUser(authentication);
        User favoriteUser = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

        // 유저의 '즐겨찾기 레파지토리'에 관심유저가 있는지 확인
        Favorite favoriteOfUser =
            favoriteRepository.findByUserAndFavoriteUser(user, favoriteUser)
            .orElseThrow(() -> new CustomException(NOT_REGISTERED_FAVORITE));
        // 차단한 경우
        if (favoriteOfUser.isBlocked()) {
            throw new CustomException(USER_IS_BLOCKED);
        } else {
            favoriteRepository.delete(favoriteOfUser);
        }
    }

    // 유저 차단
    @Transactional
    public String block(Authentication authentication, Long userId) {
        User user = getUser(authentication);
        User favoriteUser = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

        // 유저의 '즐겨찾기 레파지토리'에 관심유저가 있는지 확인
        Optional<Favorite> optionalFavoriteOfUser =
            favoriteRepository.findByUserAndFavoriteUser(user, favoriteUser);
        if (optionalFavoriteOfUser.isPresent()) {
            Favorite favoriteOfUser = optionalFavoriteOfUser.get();
            // 차단한 경우
            if (favoriteOfUser.isBlocked()) {
                throw new CustomException(USER_IS_BLOCKED);
            } else {
                // 즐겨찾기 유저인 경우
                favoriteOfUser.setBlocked(true);
                favoriteRepository.save(favoriteOfUser);
            }
        } else {
            favoriteRepository.save(Favorite.builder()
                .user(user)
                .favoriteUser(favoriteUser)
                .blocked(true)
                .build()
            );
        }

        return favoriteUser.getName() + " 님을 차단했습니다.";
    }

    // 차단 해제 -> 관심 유저도 아닌 아무런 관계가 없는 유저가 됩니다.
    @Transactional
    public String unBlock(Authentication authentication, Long userId) {
        User user = getUser(authentication);
        User favoriteUser = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

        // 유저의 '즐겨찾기 레파지토리'에 관심유저가 있는지 확인
        Favorite favoriteOfUser = favoriteRepository.findByUserAndFavoriteUser(user, favoriteUser)
                .orElseThrow(() -> new CustomException(USER_IS_NOT_BLOCKED));

        if (favoriteOfUser.isBlocked()) {
            favoriteRepository.delete(favoriteOfUser);
            return favoriteUser.getName() + " 님을 차단 해제 했습니다.";
        } else {
            throw new CustomException(USER_IS_NOT_BLOCKED);
        }
    }

    // 즐겨찾기 목록 조회
    public List<UserInfoDto> favoriteList(Authentication authentication) {
        User user = getUser(authentication);
        return favoriteRepository.findByUserAndBlocked(user, false)
            .stream()
            .map(Favorite::getFavoriteUser)
            .map(UserInfoDto::from)
            .collect(Collectors.toList());
    }

    // 차단 유저 목록 조회
    public List<UserInfoDto> blockedList(Authentication authentication) {
        User user = getUser(authentication);
        return favoriteRepository.findByUserAndBlocked(user, true)
            .stream()
            .map(Favorite::getFavoriteUser)
            .map(UserInfoDto::from)
            .collect(Collectors.toList());
    }

    private User getUser(Authentication authentication) {
        PrincipalDetails details = (PrincipalDetails) authentication.getPrincipal();
        return details.getUser();
    }
}
