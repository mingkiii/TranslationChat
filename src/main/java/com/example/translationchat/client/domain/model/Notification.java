package com.example.translationchat.client.domain.model;

import com.example.translationchat.client.domain.form.NotificationForm;
import com.example.translationchat.client.domain.type.ContentType;
import com.example.translationchat.common.model.BaseEntity;
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
import org.hibernate.envers.AuditOverride;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@AuditOverride(forClass = BaseEntity.class)
public class Notification extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column
    private Long args;

    @Column
    private Long roomId;

    @Column
    @Enumerated(EnumType.STRING)
    private ContentType content;

    public static Notification from(NotificationForm form) {
        return Notification.builder()
            .user(form.getUser())
            .args(form.getArgs())
            .roomId(form.getRoomId())
            .content(form.getContentType())
            .build();
    }
}
