package com.example.translationchat.client.domain.repository;

import com.example.translationchat.client.domain.model.Favorite;
import com.example.translationchat.client.domain.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserAndFavorite(User user, User favorite);
    List<Favorite> findByUserAndBlocked(User user, boolean isBlocked);
}
