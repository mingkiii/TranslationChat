package com.example.translationchat.domain.block.dto;

import com.example.translationchat.domain.type.ActiveStatus;
import com.example.translationchat.domain.type.Language;
import com.example.translationchat.domain.type.Nationality;
import com.example.translationchat.domain.block.entity.Block;
import com.example.translationchat.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockDto {
  private Long blockId;
  private Long blockUserId;
  private String name;
  private Nationality nationality;
  private Language language;
  private ActiveStatus status;

  public static BlockDto from(Block block) {
    User blockUser = block.getBlockUser();
    return BlockDto.builder()
        .blockId(block.getId())
        .blockUserId(blockUser.getId())
        .name(blockUser.getName())
        .nationality(blockUser.getNationality())
        .language(blockUser.getLanguage())
        .status(blockUser.getStatus())
        .build();
  }
}
