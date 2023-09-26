package com.example.translationchat.domain.block.repository;

import com.example.translationchat.domain.block.entity.Block;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {

    boolean existsByUserIdAndBlockUserId(Long userId, Long blockUserId);
    List<Block> findAllByUserId(Long userId);
}
