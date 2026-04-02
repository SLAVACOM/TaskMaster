package com.slavacom.authservice.service;

import com.slavacom.authservice.event.UserCreatedEvent;
import com.slavacom.authservice.event.UserLoginEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventProducer {

//    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.user-created:user-created}")
    private String userCreatedTopic;

	@Value("${kafka.topic.user-login:user-login}")
	private static final String userLoginTopic = "user-login";

    public void sendUserCreatedEvent(UserCreatedEvent event) {
        log.info("Sending UserCreatedEvent to Kafka topic {}: {}", userCreatedTopic, event);
//        kafkaTemplate.send(userCreatedTopic, event.userId().toString(), event);
        log.info("UserCreatedEvent sent successfully for userId: {}", event.userId());
    }

	public void sendUserLoginEvent(UserLoginEvent event) {
//		kafkaTemplate.send(userLoginTopic, event.userId().toString(), event);
	}


}

