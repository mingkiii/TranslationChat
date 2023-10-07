package com.example.translationchat.domain.chat.entity;

import com.example.translationchat.domain.user.entity.User;
import java.time.Instant;
import javax.persistence.Entity;
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
public class RandomChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "join_user1_id")
    private User joinUser1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "join_user2_id")
    private User joinUser2;

    private Instant createdTime;
}
