package com.example.cloudtalk.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.cloudtalk.model.ProductReviewSummary;

import java.util.Optional;

public interface ProductReviewSummaryRepository extends JpaRepository<ProductReviewSummary, Long> {
    Optional<ProductReviewSummary> findByProductId(Long productId);
}
