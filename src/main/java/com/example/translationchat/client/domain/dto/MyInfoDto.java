package com.example.translationchat.client.domain.dto;

import com.example.translationchat.client.domain.model.User;
import com.example.translationchat.client.domain.type.Nationality;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyInfoDto {

    private Long id;
    private String email;
    private String name;
    private Nationality nationality;
    private String language;
    private boolean randomApproval;

    public static MyInfoDto from(User user) {
        return MyInfoDto.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .nationality(user.getNationality())
            .language(user.getLanguage().getDisplayName())
            .randomApproval(user.isRandomApproval())
            .build();
    }
}
