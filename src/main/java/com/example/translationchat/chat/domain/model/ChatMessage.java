package com.example.translationchat.chat.domain.model;

import com.example.translationchat.client.domain.model.User;
import com.example.translationchat.client.domain.type.Language;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String message;

    @Enumerated(EnumType.STRING)
    private Language language;

    private String transMessage;

    @Enumerated(EnumType.STRING)
    private Language transLanguage;

    @CreatedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    public static ChatMessage createTrans(User user, ChatRoom chatRoom, String message, String transMessage, Language transLanguage) {
        return ChatMessage.builder()
            .user(user)
            .chatRoom(chatRoom)
            .message(message)
            .language(user.getLanguage())
            .transMessage(transMessage)
            .transLanguage(transLanguage)
            .build();
    }

    public static ChatMessage create(User user, ChatRoom chatRoom, String message) {
        return ChatMessage.builder()
            .user(user)
            .chatRoom(chatRoom)
            .message(message)
            .language(user.getLanguage())
            .build();
    }
}
