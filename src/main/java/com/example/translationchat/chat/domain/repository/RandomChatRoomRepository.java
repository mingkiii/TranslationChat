package com.example.translationchat.chat.domain.repository;

import com.example.translationchat.chat.domain.model.RandomChatRoom;
import com.example.translationchat.client.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RandomChatRoomRepository extends JpaRepository<RandomChatRoom, Long> {

    boolean existsByJoinUser1OrJoinUser2(User joinUser1, User joinUser2);
}
