package com.example.translationchat.chat.domain.repository;

import com.example.translationchat.chat.domain.model.ChatRoomUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser, Long> {

}
