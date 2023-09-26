package com.example.translationchat.domain.chat.repository;

import com.example.translationchat.domain.chat.entity.ChatMessage;
import com.example.translationchat.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    void deleteByChatRoom(ChatRoom room);
}
