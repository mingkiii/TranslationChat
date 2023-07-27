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
public class FriendInfoDto {

    private String name;
    private Nationality nationality;
    private String language;
    private boolean randomApproval;

    public static FriendInfoDto from(User user) {
        if (user == null) {
            return null;
        }

        return FriendInfoDto.builder()
            .name(user.getName())
            .nationality(user.getNationality())
            .language(user.getLanguage().getDisplayName())
            .randomApproval(user.isRandomApproval())
            .build();
    }
}
