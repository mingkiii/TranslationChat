package com.example.translationchat.chat.domain.repository;

import com.example.translationchat.chat.domain.model.ChatRoomUser;
import com.example.translationchat.client.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser, Long> {

    @Query("SELECT COUNT(cru) > 0 FROM ChatRoomUser cru WHERE cru.user = :user1 AND EXISTS (SELECT 1 FROM ChatRoomUser cru2 WHERE cru2.chatRoom = cru.chatRoom AND cru2.user = :user2)")
    boolean existsByUser(@Param("user1") User user1, @Param("user2") User user2);
}
