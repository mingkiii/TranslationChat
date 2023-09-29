package com.example.translationchat.domain.chat.controller;

import com.example.translationchat.common.papago.PapagoService;
import com.example.translationchat.domain.chat.dto.RandomChatMessageDto;
import com.example.translationchat.domain.chat.entity.RandomChatMessage;
import com.example.translationchat.domain.chat.entity.RandomChatRoom;
import com.example.translationchat.domain.chat.service.RandomChatMessageService;
import com.example.translationchat.domain.chat.service.RandomChatRoomService;
import com.example.translationchat.domain.type.Language;
import com.example.translationchat.domain.user.entity.User;
import com.example.translationchat.domain.user.service.UserService;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class RandomChatMessageController {

    private final SimpMessageSendingOperations sendingTemplate;
    private final RandomChatMessageService chatMessageService;
    private final RandomChatRoomService chatRoomService;
    private final UserService userService;
    private final PapagoService papagoService;

    @MessageMapping("/random/message/enter/{roomId}/{userId}")
    public void enterMember(@DestinationVariable("roomId") Long roomId,
        @DestinationVariable("userId") Long userId) {
        RandomChatRoom chatRoom = chatRoomService.findByRoomId(roomId);
        User senderUser = userService.findById(userId);
        String enterMessage = "WellCome " + senderUser.getName();

        sendingTemplate.convertAndSend("/sub/chat/" + chatRoom.getId(), enterMessage);
    }

    @MessageMapping("/random/message/{roomId}")
    public void sendMessage(@Payload RandomChatMessageDto message,
        @DestinationVariable("roomId") Long roomId) {
        RandomChatRoom chatRoom = chatRoomService.findByRoomId(roomId);
        User senderUser = userService.findById(message.getUserId());
        message.setLanguage(senderUser.getLanguage());

        User otherUser = Objects.equals(chatRoom.getJoinUser1().getId(), senderUser.getId())
                ? chatRoom.getJoinUser2() : chatRoom.getJoinUser1();

        Language transLanguage = otherUser.getLanguage();
        String transText = papagoService.getTransSentence(message.getText(), senderUser.getLanguage(), transLanguage);
        message.setTransLanguage(transLanguage);
        message.setTransText(transText);

        RandomChatMessage saveMessage = chatMessageService.sendMessage(chatRoom, senderUser, message);

        sendingTemplate.convertAndSend("/sub/chat/" + chatRoom.getId(), RandomChatMessageDto.from(saveMessage));
    }

    @MessageMapping("/random/message/exit/{roomId}/{userId}")
    public void exitMember(@DestinationVariable("roomId") Long roomId,
        @DestinationVariable("userId") Long userId) {
        RandomChatRoom chatRoom = chatRoomService.findByRoomId(roomId);
        User senderUser = userService.findById(userId);
        User otherUser = Objects.equals(chatRoom.getJoinUser1().getId(), senderUser.getId())
            ? chatRoom.getJoinUser2() : chatRoom.getJoinUser1();
        if (otherUser != null) {
            String exitMessage = senderUser.getName() + " has left..";

            sendingTemplate.convertAndSend("/sub/chat/" + chatRoom.getId(), exitMessage);
        }
    }

}
