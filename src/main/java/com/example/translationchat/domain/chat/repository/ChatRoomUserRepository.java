package com.example.translationchat.domain.chat.repository;

import com.example.translationchat.domain.chat.entity.ChatRoom;
import com.example.translationchat.domain.chat.entity.ChatRoomUser;
import com.example.translationchat.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser, Long> {

    @Query("SELECT COUNT(cru) > 0 FROM ChatRoomUser cru WHERE cru.user = :user1 AND EXISTS (SELECT 1 FROM ChatRoomUser cru2 WHERE cru2.chatRoom = cru.chatRoom AND cru2.user = :user2)")
    boolean existsByUser(@Param("user1") User user1, @Param("user2") User user2);

    List<ChatRoomUser> findAllByUserId(Long userId);

    Optional<ChatRoomUser> findByUserAndChatRoom(User user, ChatRoom chatRoom);
}
