package com.example.translationchat.common.kafka;

import com.example.translationchat.chat.domain.model.ChatMessage;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Consumers {
    private final Logger logger = LoggerFactory.getLogger(Consumers.class);
    @KafkaListener(topics = "${spring.kafka.template.default-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(@Payload ChatMessage chatMsg) throws Exception {
        logger.info("Consume msg : roomId : '{}', nickname :'{}', sender : '{}' ",
            chatMsg.getId(), chatMsg.getUser().getName(), chatMsg.getMessage());
        Map<String, String> msg = new HashMap<>();
        msg.put("roomNum", String.valueOf(chatMsg.getChatRoom().getId()));
        msg.put("message", chatMsg.getMessage());
        msg.put("sender", chatMsg.getUser().getName());
    }
}
