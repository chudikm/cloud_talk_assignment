package com.example.cloudtalk.service;

import java.time.Duration;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.cloudtalk.model.Review;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisCacheService {
    
    private static final int CACHE_DURATION_MINS = 10;
    private final String REVIEWS_CACHE_KEY = "reviews:";
    private final RedisTemplate<String, List<Review>> redisReviewTemplate;
    
    
    public void saveReviews(Long productId, List<Review> reviews) {
        redisReviewTemplate.opsForValue().set(REVIEWS_CACHE_KEY + productId, 
                reviews, 
                Duration.ofMinutes(CACHE_DURATION_MINS)
        );
    }
    
    public List<Review> getReviews(Long productId) {
        return redisReviewTemplate.opsForValue().get(REVIEWS_CACHE_KEY + productId);
    }
    
    public void deleteReviews(Long productId) {
        redisReviewTemplate.delete(REVIEWS_CACHE_KEY + productId);
    }
    
    
    

}
