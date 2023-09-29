package com.example.translationchat.domain.chat.controller;

import static com.example.translationchat.common.exception.ErrorCode.NOT_EXIST_CLIENT;

import com.example.translationchat.common.exception.CustomException;
import com.example.translationchat.common.papago.PapagoService;
import com.example.translationchat.domain.chat.dto.ChatMessageDto;
import com.example.translationchat.domain.chat.entity.ChatMessage;
import com.example.translationchat.domain.chat.entity.ChatRoom;
import com.example.translationchat.domain.chat.entity.ChatRoomUser;
import com.example.translationchat.domain.chat.service.ChatMessageService;
import com.example.translationchat.domain.chat.service.ChatRoomService;
import com.example.translationchat.domain.chat.service.ChatRoomUserService;
import com.example.translationchat.domain.type.Language;
import com.example.translationchat.domain.user.entity.User;
import com.example.translationchat.domain.user.service.UserService;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final SimpMessageSendingOperations sendingTemplate;
    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;
    private final ChatRoomUserService chatRoomUserService;
    private final UserService userService;
    private final PapagoService papagoService;

    @MessageMapping("/message/enter/{roomId}/{userId}")
    public void enterMember(@DestinationVariable("roomId") Long roomId,
        @DestinationVariable("userId") Long userId) {
        ChatRoom chatRoom = chatRoomService.findByRoomId(roomId);
        User senderUser = userService.findById(userId);
        String enterMessage = "WellCome " + senderUser.getName();

        sendingTemplate.convertAndSend("/sub/chat/" + chatRoom.getId(), enterMessage);
    }

    @MessageMapping("/message/{roomId}")
    public void sendMessage(@Payload ChatMessageDto message,
        @DestinationVariable("roomId") Long roomId) {
        ChatRoom chatRoom = chatRoomService.findByRoomId(roomId);
        User senderUser = userService.findById(message.getUserId());
        message.setLanguage(senderUser.getLanguage());

        User otherUser = chatRoomUserService.findByRoomId(roomId).stream()
            .filter(chatRoomUser -> !Objects.equals(chatRoomUser.getUser().getId(), senderUser.getId()))
            .findFirst().map(ChatRoomUser::getUser)
            .orElseThrow(() -> new CustomException(NOT_EXIST_CLIENT));

        Language transLanguage = otherUser.getLanguage();
        String transText = papagoService.getTransSentence(message.getText(), senderUser.getLanguage(), transLanguage);
        message.setTransLanguage(transLanguage);
        message.setTransText(transText);

        ChatMessage saveMessage = chatMessageService.sendMessage(chatRoom, senderUser, message);

        sendingTemplate.convertAndSend("/sub/chat/" + chatRoom.getId(), ChatMessageDto.from(saveMessage));
    }

    @MessageMapping("/message/exit/{roomId}/{userId}")
    public void exitMember(@DestinationVariable("roomId") Long roomId,
        @DestinationVariable("userId") Long userId) {
        ChatRoom chatRoom = chatRoomService.findByRoomId(roomId);
        User senderUser = userService.findById(userId);
        List<ChatRoomUser> chatRoomUsers = chatRoomUserService.findByRoomId(roomId);
        if (chatRoomUsers.size() > 1) {
            String exitMessage = senderUser.getName() + " has left..";

            sendingTemplate.convertAndSend("/sub/chat/" + chatRoom.getId(), exitMessage);
        }
    }

}
