package com.example.translationchat.domain.block.controller;

import static com.example.translationchat.common.exception.ErrorCode.ALREADY_REGISTERED_BLOCKED;
import static com.example.translationchat.common.exception.ErrorCode.BAD_REQUEST;

import com.example.translationchat.common.exception.CustomException;
import com.example.translationchat.common.security.principal.PrincipalDetails;
import com.example.translationchat.domain.block.dto.BlockDto;
import com.example.translationchat.domain.block.entity.Block;
import com.example.translationchat.domain.block.service.BlockService;
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
@RequestMapping("/api/blocks")
@RequiredArgsConstructor
public class BlockController {

    private final BlockService blockService;
    private final UserService userService;

    // 유저 차단 등록
    @PostMapping("/users/{targetUserId}")
    public void register(
        @AuthenticationPrincipal PrincipalDetails principal, @PathVariable("targetUserId") Long targetUserId
    ) {
        User user = userService.getUserByEmail(principal.getEmail());
        User targetUser = userService.findById(targetUserId);
        if (Objects.equals(user.getId(), targetUserId)) {
            throw new CustomException(BAD_REQUEST);
        }
        if (blockService.existsByUserIdAndBlockUserId(user.getId(), targetUserId)) {
            throw new CustomException(ALREADY_REGISTERED_BLOCKED);
        }

        blockService.create(user, targetUser);
    }

    // 차단 해제
    @DeleteMapping("/{blockId}")
    public void unblock(
        @AuthenticationPrincipal PrincipalDetails principal, @PathVariable("blockId") Long blockId
    ) {
        User user = userService.getUserByEmail(principal.getEmail());
        Block block = blockService.findById(blockId);
        if (!Objects.equals(user.getId(), block.getUser().getId())) {
            throw new CustomException(BAD_REQUEST);
        }
        blockService.delete(block);
    }

    // 차단 유저 목록 조회
    @GetMapping
    public ResponseEntity<List<BlockDto>> blockList(@AuthenticationPrincipal PrincipalDetails principal) {
        User user = userService.getUserByEmail(principal.getEmail());
        return ResponseEntity.ok(blockService.blockList(user.getId()).stream()
            .map(BlockDto::from)
            .collect(Collectors.toList())
        );
    }
}
