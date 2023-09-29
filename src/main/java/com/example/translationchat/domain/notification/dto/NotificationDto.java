package com.example.translationchat.domain.notification.dto;

import com.example.translationchat.domain.notification.entity.Notification;
import com.example.translationchat.domain.type.ContentType;
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
    private Long notificationId;
    private Long receiverUserId;
    private Long sendUserId;
    private String sendUserName;
    private ContentType content;
    private LocalDateTime createdAt;

    public static NotificationDto from(Notification notification) {
        return NotificationDto.builder()
            .notificationId(notification.getId())
            .receiverUserId(notification.getUser().getId())
            .sendUserId(notification.getSendUserId())
            .sendUserName(notification.getSendUserName())
            .content(notification.getContent())
            .createdAt(notification.getCreatedAt())
            .build();
    }
}
