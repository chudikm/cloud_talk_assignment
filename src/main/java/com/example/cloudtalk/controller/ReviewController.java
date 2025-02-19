package com.example.cloudtalk.controller;

import org.springframework.web.bind.annotation.*;

import com.example.cloudtalk.model.Review;
import com.example.cloudtalk.service.ReviewService;

import lombok.AllArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/products/{productId}/reviews")
@AllArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;


    @GetMapping
    public List<Review> getAllReviews(@PathVariable Long productId) {
        return reviewService.getAllReviewsForProduct(productId);
    }

    @PostMapping
    public Review createReview(@PathVariable Long productId, @RequestBody Review review) {
        return reviewService.createReview(productId, review);
    }

    @PutMapping("/{reviewId}")
    public Review updateReview(@PathVariable Long reviewId, @RequestBody Review updatedReview) {
        return reviewService.updateReview(reviewId, updatedReview);
    }

    @DeleteMapping("/{reviewId}")
    public void deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
    }
}