package com.example.translationchat.domain.user.dto;

import com.example.translationchat.domain.type.ActiveStatus;
import com.example.translationchat.domain.type.Language;
import com.example.translationchat.domain.user.entity.User;
import com.example.translationchat.domain.type.Nationality;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoDto {

    private Long id;
    private String name;
    private Nationality nationality;
    private Language language;
    private ActiveStatus status;

    public static UserInfoDto from(User user) {
        return UserInfoDto.builder()
            .id(user.getId())
            .name(user.getName())
            .nationality(user.getNationality())
            .language(user.getLanguage())
            .status(user.getStatus())
            .build();
    }
}