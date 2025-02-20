package com.example.cloudtalk.messaging;

import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.connection.DefaultMessage;
import org.springframework.data.redis.connection.Message;

import com.example.cloudtalk.messaging.RedisMessagePublisher;
import com.example.cloudtalk.messaging.RedisMessageSubscriber;
import com.example.cloudtalk.messaging.dto.ReviewUpdateMessage;
import com.example.cloudtalk.service.ProductReviewSummaryService;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RedisMessageSubscriberTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ProductReviewSummaryService productReviewSummaryService;

    @Mock
    private RedisMessagePublisher publisher;

    @InjectMocks
    private RedisMessageSubscriber redisMessageSubscriber;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        redisMessageSubscriber.retryCountMap = new ConcurrentHashMap<>();
    }

    @Test
    public void testOnMessage_Success() throws Exception {
        String json = "{\"productId\": 123, \"oldReviewRating\": 4, \"newReviewRating\": 5}";
        ReviewUpdateMessage reviewUpdateMessage = new ReviewUpdateMessage(123L, 4, 5);
        Message message = new DefaultMessage("cloudtalk-reviews".getBytes(), json.getBytes(StandardCharsets.UTF_8));

        when(objectMapper.readValue(json, ReviewUpdateMessage.class)).thenReturn(reviewUpdateMessage);

        redisMessageSubscriber.onMessage(message, null);

        verify(productReviewSummaryService).updateProductReviewSummary(123L, 4, 5);
        verify(publisher, never()).publishToReviews(anyString());
        verify(publisher, never()).publishToDLQ(anyString());
    }

    @Test
    public void testOnMessage_Exception_Retry() throws Exception {
        String json = "{\"productId\": 123, \"oldReviewRating\": 4, \"newReviewRating\": 5}";
        Message message = new DefaultMessage("cloudtalk-reviews".getBytes(), json.getBytes(StandardCharsets.UTF_8));

        when(objectMapper.readValue(json, ReviewUpdateMessage.class)).thenThrow(new RuntimeException("Processing error"));

        redisMessageSubscriber.onMessage(message, null);

        verify(publisher).publishToReviews(json);
        verify(publisher, never()).publishToDLQ(anyString());
    }

    @Test
    public void testOnMessage_Exception_DLQ() throws Exception {
        String json = "{\"productId\": 123, \"oldReviewRating\": 4, \"newReviewRating\": 5}";
        Message message = new DefaultMessage("cloudtalk-reviews".getBytes(), json.getBytes(StandardCharsets.UTF_8));

        when(objectMapper.readValue(json, ReviewUpdateMessage.class)).thenThrow(new RuntimeException("Processing error"));

        // Simulate retries
        redisMessageSubscriber.onMessage(message, null);
        redisMessageSubscriber.onMessage(message, null);
        redisMessageSubscriber.onMessage(message, null);
        redisMessageSubscriber.onMessage(message, null);
        verify(publisher, times(3)).publishToReviews(json);
        verify(publisher).publishToDLQ(json);
    }
}
