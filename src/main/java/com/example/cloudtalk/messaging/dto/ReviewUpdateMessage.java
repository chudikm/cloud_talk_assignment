package com.example.cloudtalk.messaging.dto;

public record ReviewUpdateMessage(Long productId, Integer oldReviewRating, Integer newReviewRating) {}

