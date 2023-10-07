package com.example.translationchat.domain.notification.entity;

import com.example.translationchat.common.model.BaseEntity;
import com.example.translationchat.domain.notification.form.NotificationForm;
import com.example.translationchat.domain.type.ContentType;
import com.example.translationchat.domain.user.entity.User;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
public class Notification extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column
    private Long sendUserId;

    @Column
    private String sendUserName;

    @Column
    @Enumerated(EnumType.STRING)
    private ContentType content;

    public static Notification from(NotificationForm form) {
        return Notification.builder()
            .user(form.getUser())
            .sendUserId(form.getSendUserId())
            .sendUserName(form.getSendUserName())
            .content(form.getContentType())
            .build();
    }
}
