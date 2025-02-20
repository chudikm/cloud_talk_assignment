package com.example.cloudtalk.messaging;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class RedisMessagePublisher {

    private final StringRedisTemplate redisTemplate;

    private static final String CHANNEL_REVIEWS = "cloudtalk-reviews";
    private static final String CHANNEL_DQL = "cloudtalk-reviews-dlq";
    
    
    public void publishToReviews(String message) {
        redisTemplate.convertAndSend(CHANNEL_REVIEWS, message);
    }
    
    public void publishToDLQ( String message) {
        redisTemplate.convertAndSend(CHANNEL_DQL, message);
    }
}