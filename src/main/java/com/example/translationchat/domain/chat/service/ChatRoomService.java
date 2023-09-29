package com.example.translationchat.domain.chat.service;

import static com.example.translationchat.common.exception.ErrorCode.NOT_INVALID_ROOM;

import com.example.translationchat.common.exception.CustomException;
import com.example.translationchat.domain.chat.entity.ChatRoom;
import com.example.translationchat.domain.chat.repository.ChatRoomRepository;
import com.example.translationchat.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

  private final ChatRoomRepository chatRoomRepository;

  public ChatRoom findByRoomId(Long roomId) {
    return chatRoomRepository.findById(roomId)
        .orElseThrow(() -> new CustomException(NOT_INVALID_ROOM));
  }

  @Transactional
  public ChatRoom findByTitleOrCreateRoom(User user, User targetUser) {
    ChatRoom chatRoom = chatRoomRepository.findByTitle(user.getName() + "님과 " + targetUser.getName() + "님의 대화방")
        .orElse(null);
    if (chatRoom == null) {
      chatRoom = chatRoomRepository.findByTitle(targetUser.getName() + "님과 " + user.getName() + "님의 대화방")
          .orElse(null);
    }
    if (chatRoom == null) {
      chatRoom = chatRoomRepository.save(ChatRoom.builder()
          .title(user.getName() + "님과 " + targetUser.getName() + "님의 대화방")
          .build()
      );
    }
    return chatRoom;
  }

  public void delete(ChatRoom chatRoom) {
    chatRoomRepository.delete(chatRoom);
  }
}
