package com.example.translationchat.domain.favorite.controller;

import static com.example.translationchat.common.exception.ErrorCode.ALREADY_REGISTERED_BLOCKED;
import static com.example.translationchat.common.exception.ErrorCode.ALREADY_REGISTERED_FAVORITE;
import static com.example.translationchat.common.exception.ErrorCode.BAD_REQUEST;

import com.example.translationchat.common.exception.CustomException;
import com.example.translationchat.common.security.principal.PrincipalDetails;
import com.example.translationchat.domain.block.service.BlockService;
import com.example.translationchat.domain.favorite.dto.FavoriteDto;
import com.example.translationchat.domain.favorite.entity.Favorite;
import com.example.translationchat.domain.favorite.service.FavoriteService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final UserService userService;
    private final BlockService blockService;

    // 유저 즐겨찾기 등록
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
        if (favoriteService.existsByUserIdAndFavoriteUserId(user.getId(), targetUser.getId())) {
            throw new CustomException(ALREADY_REGISTERED_FAVORITE);
        }
        // 차단 유저인지 확인
        if (blockService.existsByUserIdAndBlockUserId(userId, targetUserId)) {
            throw new CustomException(ALREADY_REGISTERED_BLOCKED);
        }

        favoriteService.register(user, targetUser);
    }

    // 유저 즐겨찾기 해제
    @DeleteMapping("/{favoriteId}")
    public void delete(@AuthenticationPrincipal PrincipalDetails principal, @PathVariable("favoriteId") Long favoriteId) {
        User user = userService.getUserByEmail(principal.getEmail());
        Favorite favorite = favoriteService.findById(favoriteId);
        if (!Objects.equals(user.getId(), favorite.getUser().getId())) {
            throw new CustomException(BAD_REQUEST);
        }
        favoriteService.delete(favorite);
    }

    // 즐겨찾기 목록 조회
    @GetMapping
    public ResponseEntity<List<FavoriteDto>> favoriteList(@AuthenticationPrincipal PrincipalDetails principal) {
        User user = userService.getUserByEmail(principal.getEmail());
        return ResponseEntity.ok(favoriteService.favoriteList(user.getId()).stream()
            .map(FavoriteDto::from)
            .collect(Collectors.toList())
        );
    }
}
