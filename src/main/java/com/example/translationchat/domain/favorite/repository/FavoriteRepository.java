package com.example.translationchat.domain.favorite.repository;

import com.example.translationchat.domain.favorite.entity.Favorite;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUserIdAndFavoriteUserId(Long userId, Long favoriteUserId);
    List<Favorite> findAllByUserId(Long userId);
}
