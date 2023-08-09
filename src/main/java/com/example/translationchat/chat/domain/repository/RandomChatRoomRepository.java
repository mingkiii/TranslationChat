package com.example.translationchat.chat.domain.repository;

import com.example.translationchat.chat.domain.model.RandomChatRoom;
import com.example.translationchat.client.domain.model.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RandomChatRoomRepository extends JpaRepository<RandomChatRoom, Long> {

    boolean existsByCreateUserOrJoinUser(User user1, User user2);

    Optional<RandomChatRoom> findFirstByJoinUserIsNull();

    List<RandomChatRoom> findByJoinUserIsNullAndCreatedTimeBefore(Instant fiveMinutesAgo);
}
