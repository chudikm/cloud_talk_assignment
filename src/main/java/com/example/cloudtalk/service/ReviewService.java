package com.example.cloudtalk.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.cloudtalk.messaging.RedisMessagePublisher;
import com.example.cloudtalk.messaging.dto.ReviewUpdateMessage;
import com.example.cloudtalk.model.Product;
import com.example.cloudtalk.model.Review;
import com.example.cloudtalk.repository.ProductRepository;
import com.example.cloudtalk.repository.ReviewRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final RedisMessagePublisher publisher;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private RedisCacheService redisCacheService;


    public List<Review> getAllReviewsForProduct(Long productId) {
        
        List<Review> cachedReviews = redisCacheService.getReviews(productId);
        if (cachedReviews != null) {
            log.info("USING CACHED VALUES FOR REVIEWS");
            return cachedReviews; // Return cached reviews
        }

        List<Review> reviews = reviewRepository.findByProductId(productId);
        redisCacheService.saveReviews(productId, reviews);
        return reviews;
    }
    
    public Optional<Review> getReviewById(Long id) {
        return reviewRepository.findById(id);
    }


    public Review createReview(Long productId, Review review) {
        return productRepository.findById(productId)
                .map(product -> {
                    review.setProduct(product);
                    Review savedReview =  reviewRepository.save(review);
                    sendReviewUpdate(productId, null, savedReview.getRating());
                    notificationService.notifyExternalService(
                            String.format("Created: productId=%d, reviewId=%d, rating=%d", 
                                    product.getId(), 
                                    review.getId(),
                                    review.getRating())
                    );
                    clearCache(review.getProduct().getId());
                    return savedReview;
                }).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public Review updateReview(Long reviewId, Review updatedReview) {
        return reviewRepository.findById(reviewId)
                .map(review -> {
                    int oldRating = review.getRating();
                    review.setFirstName(updatedReview.getFirstName());
                    review.setLastName(updatedReview.getLastName());
                    review.setReviewText(updatedReview.getReviewText());
                    review.setRating(updatedReview.getRating());
                    Review savedReview = reviewRepository.save(review);
                    sendReviewUpdate(review.getProduct().getId(), oldRating, savedReview.getRating());
                    notificationService.notifyExternalService(
                            String.format("Updated: productId=%d, reviewId=%d, rating=%d", 
                                    review.getProduct().getId(), 
                                    review.getId(),
                                    review.getRating())
                    );
                    clearCache(review.getProduct().getId());
                    return savedReview;
                }).orElseThrow(() -> new RuntimeException("Review not found"));
    }

    public void deleteReview(Long reviewId) {
        reviewRepository.findById(reviewId).ifPresent(review -> {
            sendReviewUpdate(review.getProduct().getId(), review.getRating(), null);
            notificationService.notifyExternalService(
                    String.format("Deleted: productId=%d, reviewId=%d, rating=%d", 
                            review.getProduct().getId(), 
                            review.getId(),
                            review.getRating())
            );
            reviewRepository.delete(review);
            clearCache(review.getProduct().getId());
        });
    }
    
    private void sendReviewUpdate(Long productId, Integer oldRating, Integer newRating) {
        try {
            String message = objectMapper.writeValueAsString(new ReviewUpdateMessage(productId, oldRating, newRating));
            publisher.publishToReviews(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error sending Redis message", e);
        }
    }
    
    private void clearCache(Long productId) {
        redisCacheService.deleteReviews(productId);
    }

}