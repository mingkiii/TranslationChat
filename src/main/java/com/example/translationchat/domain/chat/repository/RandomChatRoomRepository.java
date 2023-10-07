package com.example.translationchat.domain.chat.repository;

import com.example.translationchat.domain.chat.entity.RandomChatRoom;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RandomChatRoomRepository extends JpaRepository<RandomChatRoom, Long> {

    boolean existsByJoinUser1IdOrJoinUser2Id(Long joinUser1Id, Long joinUser2Id);
    Optional<RandomChatRoom> findByJoinUser1IdOrJoinUser2Id(Long joinUser1Id, Long joinUser2Id);
}
