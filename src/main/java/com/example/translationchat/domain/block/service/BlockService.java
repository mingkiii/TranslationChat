package com.example.translationchat.domain.block.service;

import static com.example.translationchat.common.exception.ErrorCode.NOT_FOUND_BLOCK;

import com.example.translationchat.common.exception.CustomException;
import com.example.translationchat.domain.block.entity.Block;
import com.example.translationchat.domain.block.repository.BlockRepository;
import com.example.translationchat.domain.user.entity.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BlockService {

    private final BlockRepository blockRepository;

    public Block findById(Long blockId) {
        return blockRepository.findById(blockId)
            .orElseThrow(() -> new CustomException(NOT_FOUND_BLOCK));
    }

    public boolean existsByUserIdAndBlockUserId(Long userId, Long blockUserId) {
        return blockRepository.existsByUserIdAndBlockUserId(userId, blockUserId);
    }

    // 차단유저 등록
    @Transactional
    public void create(User user, User targetUser) {
        blockRepository.save(Block.builder()
            .user(user)
            .blockUser(targetUser)
            .build()
        );
    }

    // 차단 해제
    @Transactional
    public void delete(Block block) {
        blockRepository.delete(block);
    }

    // 즐겨찾기 목록 조회
    public List<Block> blockList(Long userId) {
        return blockRepository.findAllByUserId(userId);
    }
}
