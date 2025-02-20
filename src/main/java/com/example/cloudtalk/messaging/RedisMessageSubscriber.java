package com.example.cloudtalk.messaging;

import java.nio.charset.StandardCharsets;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import com.example.cloudtalk.messaging.dto.ReviewUpdateMessage;
import com.example.cloudtalk.service.ProductReviewSummaryService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class RedisMessageSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private ProductReviewSummaryService productReviewSummaryService;
    
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String json = new String(message.getBody(), StandardCharsets.UTF_8);
            ReviewUpdateMessage reviewUpdate = objectMapper.readValue(json, ReviewUpdateMessage.class);
            System.out.println("Received Review Update -> Product ID: " + reviewUpdate.productId() +
                    ", Old Rating: " + reviewUpdate.oldReviewRating() +
                    ", New Rating: " + reviewUpdate.newReviewRating());
            productReviewSummaryService.updateProductReviewSummary(reviewUpdate.productId(), 
                    reviewUpdate.oldReviewRating(), 
                    reviewUpdate.newReviewRating());
        } catch (Exception e) {
            System.err.println("Error processing Redis message: " + e.getMessage());
        }
    }
}