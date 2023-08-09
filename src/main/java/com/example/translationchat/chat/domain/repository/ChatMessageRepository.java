package com.example.translationchat.chat.domain.repository;

import com.example.translationchat.chat.domain.model.ChatMessage;
import com.example.translationchat.chat.domain.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    void deleteByChatRoom(ChatRoom room);
}
