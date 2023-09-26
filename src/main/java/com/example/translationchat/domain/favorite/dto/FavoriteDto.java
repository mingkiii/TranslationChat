package com.example.translationchat.domain.favorite.dto;

import com.example.translationchat.domain.type.ActiveStatus;
import com.example.translationchat.domain.type.Language;
import com.example.translationchat.domain.type.Nationality;
import com.example.translationchat.domain.favorite.entity.Favorite;
import com.example.translationchat.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteDto {
  private Long favoriteId;
  private Long favoriteUserId;
  private String name;
  private Nationality nationality;
  private Language language;
  private ActiveStatus status;

  public static FavoriteDto from(Favorite favorite) {
    User favoriteUser = favorite.getFavoriteUser();
    return FavoriteDto.builder()
        .favoriteId(favorite.getId())
        .favoriteUserId(favoriteUser.getId())
        .name(favoriteUser.getName())
        .nationality(favoriteUser.getNationality())
        .language(favoriteUser.getLanguage())
        .status(favoriteUser.getStatus())
        .build();
  }
}
