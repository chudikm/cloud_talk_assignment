package com.example.cloudtalk.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.cloudtalk.messaging.dto.ReviewUpdateMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewKafkaPublisher {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    public void publishReviewEvent(Long productId, Integer oldRating, Integer newRating) throws Exception {
        String message = objectMapper.writeValueAsString(
                new ReviewUpdateMessage(
                        productId, 
                        oldRating, 
                        newRating));
        kafkaTemplate.send("cloudtalk-reviews", message);
    }
}
