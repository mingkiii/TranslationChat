package com.example.translationchat.domain.chat.repository;

import com.example.translationchat.domain.chat.entity.ChatRoomUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser, Long> {

    List<ChatRoomUser> findAllByUserId(Long userId);

    List<ChatRoomUser> findAllByChatRoomId(Long chatRoomId);

    Optional<ChatRoomUser> findByUserIdAndChatRoomId(Long userId, Long chatRoomId);
}
