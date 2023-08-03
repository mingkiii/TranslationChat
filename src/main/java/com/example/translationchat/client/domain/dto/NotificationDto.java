package com.example.translationchat.client.domain.dto;

import com.example.translationchat.client.domain.model.Notification;
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
    private Long args;
    private String content;
    private LocalDateTime createdAt;

    public static NotificationDto from(Notification notification) {
        return NotificationDto.builder()
            .id(notification.getId())
            .args(notification.getArgs())
            .content(notification.getContent().getDisplayName())
            .createdAt(notification.getCreatedAt())
            .build();
    }
}
