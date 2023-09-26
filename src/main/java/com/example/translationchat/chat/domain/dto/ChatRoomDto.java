package com.example.translationchat.chat.domain.dto;

import com.example.translationchat.domain.chat.entity.ChatRoom;
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
public class ChatRoomDto {
    private Long id;
    private String title;

    public static ChatRoomDto from(ChatRoom chatRoom) {
        return ChatRoomDto.builder()
            .id(chatRoom.getId())
            .title(chatRoom.getTitle())
            .build();
    }
}
