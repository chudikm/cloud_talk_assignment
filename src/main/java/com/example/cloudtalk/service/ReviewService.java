package com.example.cloudtalk.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.cloudtalk.model.Review;
import com.example.cloudtalk.repository.ProductRepository;
import com.example.cloudtalk.repository.ReviewRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Transactional

public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;


    public List<Review> getAllReviewsForProduct(Long productId) {
        return reviewRepository.findByProductId(productId);
    }

    public Review createReview(Long productId, Review review) {
        return productRepository.findById(productId)
                .map(product -> {
                    review.setProduct(product);
                    return reviewRepository.save(review);
                }).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public Review updateReview(Long reviewId, Review updatedReview) {
        return reviewRepository.findById(reviewId)
                .map(review -> {
                    review.setFirstName(updatedReview.getFirstName());
                    review.setLastName(updatedReview.getLastName());
                    review.setReviewText(updatedReview.getReviewText());
                    review.setRating(updatedReview.getRating());
                    return reviewRepository.save(review);
                }).orElseThrow(() -> new RuntimeException("Review not found"));
    }

    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }
}