package com.example.cloudtalk.service;

import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.cloudtalk.messaging.RedisMessagePublisher;
import com.example.cloudtalk.messaging.dto.ReviewUpdateMessage;
import com.example.cloudtalk.model.Product;
import com.example.cloudtalk.model.Review;
import com.example.cloudtalk.repository.ProductRepository;
import com.example.cloudtalk.repository.ReviewRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private RedisMessagePublisher publisher;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private RedisCacheService redisCacheService;

    @InjectMocks
    private ReviewService reviewService;

    private Product product;
    private Review review;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        product = new Product();
        product.setId(1L);

        review = new Review();
        review.setId(1L);
        review.setProduct(product);
        review.setRating(5);
    }

    @Test
    void testGetAllReviewsForProduct() {
        when(redisCacheService.getReviews(1L)).thenReturn(null);
        when(reviewRepository.findByProductId(1L)).thenReturn(List.of(review));

        List<Review> reviews = reviewService.getAllReviewsForProduct(1L);

        assertNotNull(reviews);
        assertEquals(1, reviews.size());
        verify(redisCacheService).saveReviews(1L, reviews);
    }

    @Test
    void testGetReviewById() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        Optional<Review> foundReview = reviewService.getReviewById(1L);

        assertTrue(foundReview.isPresent());
        assertEquals(5, foundReview.get().getRating());
    }

    @Test
    void testCreateReview() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(reviewRepository.save(review)).thenReturn(review);

        Review createdReview = reviewService.createReview(1L, review);

        assertNotNull(createdReview);
        assertEquals(5, createdReview.getRating());
        verify(notificationService).notifyExternalService(anyString());
        verify(redisCacheService).deleteReviews(1L);
    }

    @Test
    void testUpdateReview() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(review)).thenReturn(review);

        review.setRating(4);
        Review updatedReview = reviewService.updateReview(1L, review);

        assertNotNull(updatedReview);
        assertEquals(4, updatedReview.getRating());
        verify(notificationService).notifyExternalService(anyString());
        verify(redisCacheService).deleteReviews(1L);
    }

    @Test
    void testDeleteReview() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        doNothing().when(reviewRepository).delete(review);

        reviewService.deleteReview(1L);

        verify(reviewRepository, times(1)).delete(review);
        verify(notificationService).notifyExternalService(anyString());
        verify(redisCacheService).deleteReviews(1L);
    }
}
