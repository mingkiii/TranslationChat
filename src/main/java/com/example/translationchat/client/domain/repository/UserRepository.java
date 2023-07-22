package com.example.translationchat.client.domain.repository;

import com.example.translationchat.client.domain.model.User;
import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    User findByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    User findByName(String name);
}
