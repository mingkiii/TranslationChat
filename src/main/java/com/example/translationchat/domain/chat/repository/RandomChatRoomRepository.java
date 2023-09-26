package com.example.translationchat.domain.chat.repository;

import com.example.translationchat.domain.chat.entity.RandomChatRoom;
import com.example.translationchat.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RandomChatRoomRepository extends JpaRepository<RandomChatRoom, Long> {

    boolean existsByJoinUser1OrJoinUser2(User joinUser1, User joinUser2);
}
