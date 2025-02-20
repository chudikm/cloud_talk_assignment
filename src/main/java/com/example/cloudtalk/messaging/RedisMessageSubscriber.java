package com.example.cloudtalk.messaging;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import com.example.cloudtalk.messaging.dto.ReviewUpdateMessage;
import com.example.cloudtalk.service.ProductReviewSummaryService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisMessageSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final ProductReviewSummaryService productReviewSummaryService;
    private final RedisMessagePublisher publisher;
    
    private static final int MAX_RETRIES = 3;

    ConcurrentMap<String, Integer> retryCountMap = new ConcurrentHashMap<>();
    
    /**
     * this is poorman's implementation of DLQ handling, we can use Redis Streams or Kafka or RabitMQ instead
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String json = new String(message.getBody(), StandardCharsets.UTF_8);
        
        try {
            // Process the message
            processMessage(json);
            // Remove the message from the retry map on successful processing
            retryCountMap.remove(json);
        } catch (Exception e) {
            log.error("Exception caught: {} ", e.getMessage());
            int retryCount = retryCountMap.getOrDefault(json, 0);
            if (retryCount < MAX_RETRIES) {
                // Increment the retry count and re-publish the message to the original channel
                retryCountMap.put(json, retryCount + 1);
                publisher.publishToReviews(json);
            } else {
                // Send to DLQ after max retries
                publisher.publishToDLQ(json);
                // Remove the message from the retry map
                retryCountMap.remove(json);
            }
        }
    }

    private void processMessage(String json) throws Exception {

        ReviewUpdateMessage reviewUpdate = objectMapper.readValue(json, ReviewUpdateMessage.class);
        System.out.println("Received Review Update -> Product ID: " + reviewUpdate.productId() + ", Old Rating: "
                + reviewUpdate.oldReviewRating() + ", New Rating: " + reviewUpdate.newReviewRating());
        productReviewSummaryService.updateProductReviewSummary(reviewUpdate.productId(), reviewUpdate.oldReviewRating(),
                reviewUpdate.newReviewRating());

    }
}