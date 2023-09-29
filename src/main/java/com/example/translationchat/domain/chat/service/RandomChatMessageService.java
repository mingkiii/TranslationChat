package com.example.translationchat.domain.chat.service;

import com.example.translationchat.domain.chat.dto.RandomChatMessageDto;
import com.example.translationchat.domain.chat.entity.RandomChatMessage;
import com.example.translationchat.domain.chat.entity.RandomChatRoom;
import com.example.translationchat.domain.chat.repository.RandomChatMessageRepository;
import com.example.translationchat.domain.user.entity.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RandomChatMessageService {

    private final RandomChatMessageRepository chatMessageRepository;

    public RandomChatMessage sendMessage(RandomChatRoom chatRoom, User senderUser, RandomChatMessageDto message) {
        return chatMessageRepository.save(RandomChatMessage.of(chatRoom, senderUser, message));
    }

    public void deleteOfChatRoomId(Long chatRoomId) {
        List<RandomChatMessage> chatMessages = chatMessageRepository.findAllByChatRoomId(chatRoomId);
        chatMessageRepository.deleteAll(chatMessages);
    }
}
