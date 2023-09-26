package com.example.translationchat.domain.favorite.service;

import static com.example.translationchat.common.exception.ErrorCode.NOT_FOUND_FAVORITE;

import com.example.translationchat.common.exception.CustomException;
import com.example.translationchat.domain.favorite.entity.Favorite;
import com.example.translationchat.domain.favorite.repository.FavoriteRepository;
import com.example.translationchat.domain.user.entity.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;

    public Favorite findById(Long favoriteId) {
        return favoriteRepository.findById(favoriteId)
            .orElseThrow(() -> new CustomException(NOT_FOUND_FAVORITE));
    }

    public boolean existsByUserIdAndFavoriteUserId(Long userId, Long favoriteId) {
        return favoriteRepository.existsByUserIdAndFavoriteUserId(userId, favoriteId);
    }

    // 즐겨찾기 등록
    @Transactional
    public void register(User user, User targetUser) {
        favoriteRepository.save(Favorite.builder()
                .user(user)
                .favoriteUser(targetUser)
                .build()
        );
    }

    // 즐겨찾기 삭제
    @Transactional
    public void delete(Favorite favorite) {
        favoriteRepository.delete(favorite);
    }

    // 즐겨찾기 목록 조회
    public List<Favorite> favoriteList(Long userId) {
        return favoriteRepository.findAllByUserId(userId);
    }
}
