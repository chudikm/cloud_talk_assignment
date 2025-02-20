package com.example.cloudtalk;

import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableAsync;

import com.example.cloudtalk.model.ProductReviewSummary;
import com.example.cloudtalk.model.Review;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
@EnableAsync
public class CloudtalkApplication {

    @Bean
    public RedisTemplate<String, List<Review>> redisReviewTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, List<Review>> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // Configure Jackson serializer for lists of Review
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); 

        // Use JSON Serialization
        Jackson2JsonRedisSerializer<List<Review>> serializer = new Jackson2JsonRedisSerializer<>(
                objectMapper.getTypeFactory().constructCollectionType(
                        List.class, Review.class)
        );
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }
    
    @Bean
    public RedisTemplate<String, ProductReviewSummary> redisProductReviewSummaryTemplate(
            RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, ProductReviewSummary> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // Configure Jackson serializer for lists of Review
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); 

        // Use JSON Serialization
        Jackson2JsonRedisSerializer<ProductReviewSummary> serializer = new Jackson2JsonRedisSerializer<>(
                ProductReviewSummary.class
        );
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }
    
    
    public static void main(String[] args) {
        SpringApplication.run(CloudtalkApplication.class, args);
    }
	
}
