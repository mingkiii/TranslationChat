package com.example.translationchat.common.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class Producers {
    private final Logger logger = LoggerFactory.getLogger(Producers.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    @Value("${spring.kafka.template.default-topic}")
    private String topicName;

    public void produceMessage(Long roomId, String payload) {
        logger.info("Topic : '{}' to Payload : '{}'", topicName + roomId, payload);
        kafkaTemplate.send(topicName + roomId, payload);
    }
}
