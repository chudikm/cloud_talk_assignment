package com.example.cloudtalk.service;

import com.example.cloudtalk.model.Product;
import com.example.cloudtalk.model.ProductReviewSummary;
import com.example.cloudtalk.repository.ProductRepository;
import com.example.cloudtalk.repository.ProductReviewSummaryRepository;
import com.example.cloudtalk.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class ProductReviewSummaryService {

    private final ProductRepository productRepository;
    private final ProductReviewSummaryRepository summaryRepository;
    private final RedisCacheService redisCacheService;

    @Transactional
    public void updateProductReviewSummary(Long productId, Integer oldRating, Integer newRating) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductReviewSummary summary = summaryRepository.findByProductId(productId)
                .orElseGet(() -> new ProductReviewSummary(null, product, BigDecimal.ZERO, 0));

        int numReviews = summary.getNumberOfReviews();
        BigDecimal avgRating = summary.getAverageReview();

        // If it's a new review
        if (oldRating == null && newRating != null) {
            avgRating = avgRating.multiply(BigDecimal.valueOf(numReviews)) // sum of previous ratings
                    .add(BigDecimal.valueOf(newRating)) // add new rating
                    .divide(BigDecimal.valueOf(numReviews + 1), 2, RoundingMode.HALF_UP); // new avg
            numReviews++;

        // If a review is deleted
        } else if (newRating == null && oldRating != null) {
            if (numReviews > 1) {
                avgRating = avgRating.multiply(BigDecimal.valueOf(numReviews))
                        .subtract(BigDecimal.valueOf(oldRating))
                        .divide(BigDecimal.valueOf(numReviews - 1), 2, RoundingMode.HALF_UP);
                numReviews--;
            } else {
                avgRating = BigDecimal.ZERO;
                numReviews = 0;
            }

        // If a review is updated
        } else if (oldRating != null && newRating != null) {
            BigDecimal previousAvg = avgRating.multiply(BigDecimal.valueOf(numReviews))
                    .subtract(BigDecimal.valueOf(oldRating))
                    .divide(BigDecimal.valueOf(numReviews - 1), 2, RoundingMode.HALF_UP);
            avgRating = previousAvg.add(BigDecimal.valueOf(newRating))
                    .divide(BigDecimal.valueOf(numReviews), 2, RoundingMode.HALF_UP);
        }

        summary.setAverageReview(avgRating);
        summary.setNumberOfReviews(numReviews);
        summaryRepository.save(summary);
        redisCacheService.deleteProductReviewSummary(productId);
    }
}
