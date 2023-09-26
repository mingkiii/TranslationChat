package com.example.translationchat.domain.notification.dto;

import com.example.translationchat.domain.notification.entity.Notification;
import com.example.translationchat.domain.user.entity.User;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {
    private Long id;
    private User user;
    private Long args;
    private Long roomId;
    private String content;
    private LocalDateTime createdAt;

    public static NotificationDto from(Notification notification) {
        return NotificationDto.builder()
            .id(notification.getId())
            .user(notification.getUser())
            .args(notification.getArgs())
            .roomId(notification.getRoomId())
            .content(notification.getContent().getDisplayName())
            .createdAt(notification.getCreatedAt())
            .build();
    }
}
