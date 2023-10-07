package com.example.translationchat.domain.friend.dto;

import com.example.translationchat.domain.type.ActiveStatus;
import com.example.translationchat.domain.type.Language;
import com.example.translationchat.domain.type.Nationality;
import com.example.translationchat.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendDto {
  private Long friendId;
  private Long friendUserId;
  private String friendName;
  private Nationality nationality;
  private Language language;
  private ActiveStatus status;

  public static FriendDto of(Long friendId,User targetUser) {
    return FriendDto.builder()
        .friendId(friendId)
        .friendUserId(targetUser.getId())
        .friendName(targetUser.getName())
        .nationality(targetUser.getNationality())
        .language(targetUser.getLanguage())
        .status(targetUser.getStatus())
        .build();
  }
}
