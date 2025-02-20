package com.example.cloudtalk.messaging;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import com.example.cloudtalk.messaging.dto.ReviewUpdateMessage;
import com.example.cloudtalk.service.ProductReviewSummaryService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewKafkaConsumer {
    
    private final ObjectMapper objectMapper;
    private final ProductReviewSummaryService productReviewSummaryService;
    
    @KafkaListener(topics = "cloudtalk-reviews", groupId = "review-consumers")
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) throws Exception {
        log.info("Consumed message: {}", record.value());
        String json = record.value();
        processMessage(json);
        acknowledgment.acknowledge();
    }
    
    private void processMessage(String json) throws Exception {
        ReviewUpdateMessage reviewUpdate = objectMapper.readValue(json, ReviewUpdateMessage.class);
        productReviewSummaryService.updateProductReviewSummary(reviewUpdate.productId(), reviewUpdate.oldReviewRating(),
                reviewUpdate.newReviewRating());
    }
}