package com.example.translationchat.common.kafka.service;

import java.util.Collections;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaTopicService {

    private final KafkaAdmin kafkaAdmin;

    public KafkaTopicService(KafkaAdmin kafkaAdmin) {
        this.kafkaAdmin = kafkaAdmin;
    }

    public void deleteTopic(String topicName) {
        Properties properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaAdmin.getConfigurationProperties().get(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG));

        try (AdminClient adminClient = AdminClient.create(properties)) {
            DeleteTopicsResult deleteTopicsResult =
                adminClient.deleteTopics(Collections.singleton(topicName));

            deleteTopicsResult.topicNameValues();
            log.info("Topic " + topicName + " deleted successfully");
        }
    }
}
