package com.example.translationchat.domain.user.entity;

import com.example.translationchat.common.model.BaseEntity;
import com.example.translationchat.domain.type.ActiveStatus;
import com.example.translationchat.domain.type.Language;
import com.example.translationchat.domain.type.Nationality;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
public class User extends BaseEntity {
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

    private int warningCount;

    @Column
    private boolean randomApproval;
}
