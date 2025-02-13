package dev.ashu.userservice.config;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerConfig {

    private KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerConfig(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // sendMessage will push the message to the given event in kafka
    public void sendMessage(String topic, String message) {
        kafkaTemplate.send(topic, message);

        /*
        Why message being sent to kafka in String?

        --> message is sent in a serialized way
 We'll have to serialize the message before sending to kafka
        objectMapper
         */
    }
}
