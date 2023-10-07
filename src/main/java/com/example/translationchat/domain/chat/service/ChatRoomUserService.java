package com.example.translationchat.domain.chat.service;

import static com.example.translationchat.common.exception.ErrorCode.NOT_EXIST_CLIENT;

import com.example.translationchat.common.exception.CustomException;
import com.example.translationchat.domain.chat.entity.ChatRoom;
import com.example.translationchat.domain.chat.entity.ChatRoomUser;
import com.example.translationchat.domain.chat.repository.ChatRoomUserRepository;
import com.example.translationchat.domain.user.entity.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRoomUserService {

    private final ChatRoomUserRepository chatRoomUserRepository;

    // 대화방 목록 조회
    public List<ChatRoomUser> findAllByUserId(Long userId) {
        return chatRoomUserRepository.findAllByUserId(userId);
    }

    public void enterChatRoom(ChatRoom room, User user, User targetUser) {
        // 대화방에 속한 모든 ChatRoomUser 조회
        List<ChatRoomUser> chatRoomUsers = findByRoomId(room.getId());

        // 대화방에 유저와 타겟 유저가 이미 존재하는지 확인
        boolean userExists = false;
        boolean targetUserExists = false;

        for (ChatRoomUser chatRoomUser : chatRoomUsers) {
            if (chatRoomUser.getUser().equals(user)) {
                userExists = true;
            }
            if (chatRoomUser.getUser().equals(targetUser)) {
                targetUserExists = true;
            }
        }

        // 유저가 대화방에 존재하지 않는 경우, ChatRoomUser 엔티티를 생성하여 추가
        if (!userExists) {
            ChatRoomUser userChatRoomUser = ChatRoomUser.builder()
                .user(user)
                .chatRoom(room)
                .build();
            chatRoomUserRepository.save(userChatRoomUser);
        }

        // 타겟 유저가 대화방에 존재하지 않는 경우, ChatRoomUser 엔티티를 생성하여 추가
        if (!targetUserExists) {
            ChatRoomUser targetUserChatRoomUser = ChatRoomUser.builder()
                .user(targetUser)
                .chatRoom(room)
                .build();
            chatRoomUserRepository.save(targetUserChatRoomUser);
        }
    }

    public void exit(ChatRoom room, User user) {
        chatRoomUserRepository.delete(findByRoomAndUser(room, user));
    }

    public List<ChatRoomUser> findByRoomId(Long roomId) {
        return chatRoomUserRepository.findAllByChatRoomId(roomId);
    }

    public ChatRoomUser findByRoomAndUser(ChatRoom room, User user) {
        return chatRoomUserRepository.findByUserIdAndChatRoomId(user.getId(), room.getId())
            .orElseThrow(() -> new CustomException(NOT_EXIST_CLIENT));
    }
}
