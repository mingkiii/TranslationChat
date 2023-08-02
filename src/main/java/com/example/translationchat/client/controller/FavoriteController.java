package com.example.translationchat.client.controller;

import com.example.translationchat.client.domain.dto.UserInfoDto;
import com.example.translationchat.client.service.FavoriteService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ws/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    // 관심 유저 등록
    @PostMapping("/register")
    public ResponseEntity<String> register(
        Authentication authentication, @RequestParam Long userId
    ) {
        return ResponseEntity.ok(favoriteService.register(authentication, userId));
    }

    // 유저 차단
    @PutMapping("/block")
    public ResponseEntity<String> block(
        Authentication authentication, @RequestParam Long userId
    ) {
        return ResponseEntity.ok(favoriteService.block(authentication, userId));
    }

    // 차단 해제
    @DeleteMapping("/unblock")
    public ResponseEntity<String> unblock(
        Authentication authentication, @RequestParam Long userId
    ) {
        return ResponseEntity.ok(favoriteService.unBlock(authentication, userId));
    }

    // 즐겨찾기 목록 조회
    @GetMapping("/favorite-list")
    public ResponseEntity<List<UserInfoDto>> favoriteList(Authentication authentication) {
        return ResponseEntity.ok(favoriteService.favoriteList(authentication));
    }

    // 차단한 친구목록 조회
    @GetMapping("/blocked-list")
    public ResponseEntity<List<UserInfoDto>> blockedList(Authentication authentication) {
        return ResponseEntity.ok(favoriteService.blockedList(authentication));
    }

    // 관심 유저 삭제
    @DeleteMapping
    public void delete(Authentication authentication, @RequestParam Long userId) {
        favoriteService.delete(authentication, userId);
    }
}
