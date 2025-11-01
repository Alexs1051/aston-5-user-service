package org.aston.learning.stage2.service;

import org.aston.learning.stage2.event.UserEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserEventPublisher {

    private static final String TOPIC = "user-events";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void publishUserCreated(String email, String userName) {
        UserEvent event = new UserEvent("USER_CREATED", email, userName);
        kafkaTemplate.send(TOPIC, event);
    }

    public void publishUserDeleted(String email, String userName) {
        UserEvent event = new UserEvent("USER_DELETED", email, userName);
        kafkaTemplate.send(TOPIC, event);
    }
}