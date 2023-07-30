package com.example.translationchat.client.domain.repository;

import com.example.translationchat.client.domain.model.Notification;
import com.example.translationchat.client.domain.model.User;
import com.example.translationchat.client.domain.type.ContentType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByUser(User user);
    Long countByUser(User user);
    Optional<Notification> findByArgsAndContent(String args, ContentType contentType);
}
