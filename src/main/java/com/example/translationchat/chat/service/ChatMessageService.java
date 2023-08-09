package com.example.translationchat.chat.service;

import static com.example.translationchat.common.exception.ErrorCode.NOT_EXIST_CLIENT;
import static com.example.translationchat.common.exception.ErrorCode.NOT_INVALID_ROOM;

import com.example.translationchat.chat.domain.model.ChatMessage;
import com.example.translationchat.chat.domain.model.ChatRoom;
import com.example.translationchat.chat.domain.repository.ChatMessageRepository;
import com.example.translationchat.chat.domain.repository.ChatRoomRepository;
import com.example.translationchat.chat.domain.request.ChatMessageRequest;
import com.example.translationchat.client.domain.model.User;
import com.example.translationchat.client.domain.type.Language;
import com.example.translationchat.common.exception.CustomException;
import com.example.translationchat.common.kafka.Producers;
import com.example.translationchat.common.security.principal.PrincipalDetails;
import com.example.translationchat.common.papago.PapagoService;
import com.example.translationchat.server.handler.ChatHandler;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatMessageService {
    private final Producers producers;
    private final PapagoService papagoService;
    private final ChatHandler chatHandler;

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Async
    @Transactional
    public void sendMessage(
        Authentication authentication, Long roomId, ChatMessageRequest messageRequest
    ) {
        User user = getUser(authentication);
        Language language = user.getLanguage();
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new CustomException(NOT_INVALID_ROOM));

        // 대화방에 있는 상대방을 찾습니다.
        User otherUser = chatRoom.getOtherUser(user);
        Language transLanguage = otherUser.getLanguage();

        String message = messageRequest.getMessage();

        String transSentence = translateIfNeeded(message, language, transLanguage);
        saveChatMessage(user, chatRoom, message, transSentence, transLanguage);

        String formattedMessage = generateFormattedMessage(message, transSentence);

        producers.produceMessage(chatRoom.getId(), formattedMessage);
        socketSendMessage(roomId, formattedMessage);
    }

    public void socketSendMessage(Long roomId, String message) {
        List<WebSocketSession> roomSessions = chatHandler.getRoomIdSession(roomId);

        if (roomSessions == null) {
            log.info("해당 채팅방에 클라이언트가 없습니다.");
            throw new CustomException(NOT_EXIST_CLIENT);
        }
        for (WebSocketSession session : roomSessions) {
            if (session != null && session.isOpen()) {
                try {
                    TextMessage textMessage = new TextMessage(message);
                    session.sendMessage(textMessage);
                } catch (IOException e) {
                    // 메시지 전송 중 오류 발생시 처리
                    e.printStackTrace();
                }
            } else {
                throw new CustomException(NOT_EXIST_CLIENT);
            }
        }
    }

    private User getUser(Authentication authentication) {
        PrincipalDetails details = (PrincipalDetails) authentication.getPrincipal();
        return details.getUser();
    }

    private String translateIfNeeded(String message, Language fromLanguage, Language toLanguage) {
        if (fromLanguage != toLanguage) {
            return papagoService.getTransSentence(message, fromLanguage, toLanguage);
        }
        return null;
    }

    private void saveChatMessage(User user, ChatRoom chatRoom, String originalMessage,
        String translatedMessage, Language transLanguage) {
        if (translatedMessage != null) {
            chatMessageRepository.save(ChatMessage.createTrans(user, chatRoom, originalMessage, translatedMessage, transLanguage));
        } else {
            chatMessageRepository.save(ChatMessage.create(user, chatRoom, originalMessage));
        }
    }

    private String generateFormattedMessage(String originalMessage, String translatedMessage) {
        if (translatedMessage != null) {
            return originalMessage + "\n" + translatedMessage;
        }
        return originalMessage;
    }
}
