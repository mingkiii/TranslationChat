package com.example.translationchat.client.domain.repository;

import com.example.translationchat.client.domain.model.Friendship;
import com.example.translationchat.client.domain.model.User;
import com.example.translationchat.client.domain.type.FriendshipStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    Optional<Friendship> findByUserAndFriend(User user, User friend);
    List<Friendship> findByUserAndFriendshipStatus(User user, FriendshipStatus friendshipStatus);
}
