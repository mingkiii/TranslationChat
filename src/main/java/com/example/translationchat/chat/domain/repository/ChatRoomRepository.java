package com.example.translationchat.chat.domain.repository;

import com.example.translationchat.chat.domain.model.ChatRoom;
import com.example.translationchat.client.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    boolean existsByChatRoomUsersUserAndChatRoomUsersUser(User user1, User user2);
}
