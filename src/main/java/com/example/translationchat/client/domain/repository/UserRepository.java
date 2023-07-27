package com.example.translationchat.client.domain.repository;

import com.example.translationchat.client.domain.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {

    boolean existsByEmail(String email);

    boolean existsByName(String name);

    Optional<User> findByEmail(String email);
}
