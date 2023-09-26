package com.example.translationchat.domain.chat.entity;

import com.example.translationchat.common.model.BaseEntity;
import com.example.translationchat.domain.type.Language;
import com.example.translationchat.domain.user.entity.User;
import javax.persistence.Entity;
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

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage extends BaseEntity {

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
