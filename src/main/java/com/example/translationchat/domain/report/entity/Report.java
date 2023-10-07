package com.example.translationchat.domain.report.entity;

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
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private Long reporterUserId;

    private Instant reportTime;

    public static Report create(User target, Long reporterId) {
        return Report.builder()
            .user(target)
            .reporterUserId(reporterId)
            .reportTime(Instant.now())
            .build();
    }
}
