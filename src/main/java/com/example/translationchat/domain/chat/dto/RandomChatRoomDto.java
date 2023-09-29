package com.example.translationchat.domain.chat.dto;

import com.example.translationchat.domain.chat.entity.RandomChatRoom;
import com.example.translationchat.domain.user.entity.User;
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
public class RandomChatRoomDto {
    private Long roomId;
    private Long joinUser1Id;
    private String joinUser1Name;
    private Long joinUser2Id;
    private String joinUser2Name;


    public static RandomChatRoomDto from(RandomChatRoom chatRoom) {
        if (chatRoom == null) {
            return null;
        }
        User joinUser1 = chatRoom.getJoinUser1();
        User joinUser2 = chatRoom.getJoinUser2();

        return RandomChatRoomDto.builder()
            .roomId(chatRoom.getId())
            .joinUser1Id(joinUser1.getId())
            .joinUser1Name(joinUser1.getName())
            .joinUser2Id(joinUser2.getId())
            .joinUser2Name(joinUser2.getName())
            .build();
    }
}
