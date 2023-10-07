package com.example.translationchat.domain.friend.repository;

import com.example.translationchat.domain.friend.entity.Friend;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {

    Optional<Friend> findByUserIdAndFriendUserId(Long userId, Long friendUserId);

    List<Friend> findAllByUserId(Long userId);

    List<Friend> findAllByFriendUserId(Long friendUserId);
}
