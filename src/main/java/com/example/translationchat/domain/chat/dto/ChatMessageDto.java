package com.example.translationchat.domain.chat.dto;


import com.example.translationchat.domain.chat.entity.ChatMessage;
import com.example.translationchat.domain.type.Language;
import com.example.translationchat.domain.user.entity.User;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {

  private Long userId;
  private String userName;
  private Language language;
  private String text;
  private Language transLanguage;
  private String transText;
  private LocalDateTime sendTime;

  public static ChatMessageDto from(ChatMessage message) {
    User senderUser = message.getUser();
    return ChatMessageDto.builder()
        .userId(senderUser.getId())
        .userName(senderUser.getName())
        .language(senderUser.getLanguage())
        .text(message.getMessage())
        .transLanguage(message.getTransLanguage())
        .transText(message.getTransMessage())
        .sendTime(message.getCreatedAt())
        .build();
  }
}
