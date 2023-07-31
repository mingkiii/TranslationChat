package com.example.translationchat.client.domain.repository;

import com.example.translationchat.client.domain.model.Notification;
import com.example.translationchat.client.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findAllByUser(User user, Pageable pageable);
    Long countByUser(User user);
}
