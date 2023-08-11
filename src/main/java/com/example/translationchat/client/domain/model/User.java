package com.example.translationchat.client.domain.model;

import com.example.translationchat.chat.domain.model.ChatMessage;
import com.example.translationchat.chat.domain.model.ChatRoomUser;
import com.example.translationchat.client.domain.type.ActiveStatus;
import com.example.translationchat.client.domain.type.Language;
import com.example.translationchat.client.domain.type.Nationality;
import com.example.translationchat.common.model.BaseEntity;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
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
public class User extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Nationality nationality;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Language language;

    @Column
    @Enumerated(EnumType.STRING)
    private ActiveStatus status;

    private  int warningCount;

    @Column
    private boolean randomApproval;

    @OneToMany(mappedBy = "user")
    private List<Favorite> favoriteList;

    @OneToMany(mappedBy = "user")
    private List<ChatRoomUser> chatRoomUsers;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ChatMessage> chat;
}
