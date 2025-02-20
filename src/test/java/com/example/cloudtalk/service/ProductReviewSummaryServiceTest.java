package com.example.cloudtalk.service;

import com.example.cloudtalk.model.Product;
import com.example.cloudtalk.model.ProductReviewSummary;
import com.example.cloudtalk.repository.ProductRepository;
import com.example.cloudtalk.repository.ProductReviewSummaryRepository;
import com.example.cloudtalk.service.ProductReviewSummaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ProductReviewSummaryServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductReviewSummaryRepository summaryRepository;
    
    @Mock
    private RedisCacheService redisCacheService;

    @InjectMocks
    private ProductReviewSummaryService summaryService;

    private Product product;
    private ProductReviewSummary summary;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        product = new Product();
        product.setId(1L);

        summary = new ProductReviewSummary(1L, product, new BigDecimal("4.00"), 2);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(summaryRepository.findByProductId(1L)).thenReturn(Optional.of(summary));
    }

    @Test
    void testAddNewReview() {
        // Adding a new review with rating 5
        summaryService.updateProductReviewSummary(1L, null, 5);

        // Expected new average: (4.00 * 2 + 5) / 3 = 4.33
        assertEquals(new BigDecimal("4.33"), summary.getAverageReview());
        assertEquals(3, summary.getNumberOfReviews());

        verify(summaryRepository).save(summary);
        verify(redisCacheService).deleteProductReviewSummary(1L);
    }

    @Test
    void testUpdateReview() {
        // Updating a review from 4 to 5
        summaryService.updateProductReviewSummary(1L, 4, 5);

        // Expected new average: ((4.00 * 2 - 4) / 1 + 5) / 2 = 4.50
        assertEquals(new BigDecimal("4.50"), summary.getAverageReview());
        assertEquals(2, summary.getNumberOfReviews());

        verify(summaryRepository).save(summary);
        verify(redisCacheService).deleteProductReviewSummary(1L);
    }

    @Test
    void testDeleteReview() {
        // Deleting a review with rating 4
        summaryService.updateProductReviewSummary(1L, 4, null);

        // Expected new average: (4.00 * 2 - 4) / 1 = 4.00
        assertEquals(new BigDecimal("4.00"), summary.getAverageReview());
        assertEquals(1, summary.getNumberOfReviews());

        verify(summaryRepository).save(summary);
        verify(redisCacheService).deleteProductReviewSummary(1L);
    }

    @Test
    void testDeleteLastReview() {
        // If there is only one review left and it gets deleted
        summary.setNumberOfReviews(1);
        summary.setAverageReview(new BigDecimal("5.00"));

        summaryService.updateProductReviewSummary(1L, 5, null);

        assertEquals(BigDecimal.ZERO, summary.getAverageReview());
        assertEquals(0, summary.getNumberOfReviews());

        verify(summaryRepository).save(summary);
        verify(redisCacheService).deleteProductReviewSummary(1L);
    }
}
