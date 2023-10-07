package com.example.translationchat.domain.chat.service;

import com.example.translationchat.domain.chat.dto.ChatMessageDto;
import com.example.translationchat.domain.chat.entity.ChatMessage;
import com.example.translationchat.domain.chat.entity.ChatRoom;
import com.example.translationchat.domain.chat.repository.ChatMessageRepository;
import com.example.translationchat.domain.user.entity.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessage sendMessage(ChatRoom chatRoom, User senderUser, ChatMessageDto message) {
        return chatMessageRepository.save(ChatMessage.of(chatRoom, senderUser, message));
    }

    public void deleteOfChatRoomId(Long chatRoomId) {
        List<ChatMessage> chatMessages = chatMessageRepository.findAllByChatRoomId(chatRoomId);
        chatMessageRepository.deleteAll(chatMessages);
    }
}
