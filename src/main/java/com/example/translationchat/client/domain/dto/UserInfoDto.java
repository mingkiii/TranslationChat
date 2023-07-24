package com.example.translationchat.client.domain.dto;

import com.example.translationchat.client.domain.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoDto {

    private String email;
    private String name;
    private String nationality;
    private String language;
    private boolean randomApproval;

    public static UserInfoDto from(User user) {

        return UserInfoDto.builder()
            .email(user.getEmail())
            .name(user.getName())
            .nationality(String.valueOf(user.getNationality()))
            .language(user.getLanguage().getDisplayName())
            .randomApproval(user.isRandomApproval())
            .build();
    }
}
