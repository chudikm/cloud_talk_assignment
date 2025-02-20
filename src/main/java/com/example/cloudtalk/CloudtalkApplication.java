package com.example.cloudtalk;

import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableAsync;

import com.example.cloudtalk.messaging.RedisDLQMessageSubscriber;
import com.example.cloudtalk.messaging.RedisMessageSubscriber;
import com.example.cloudtalk.model.Review;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
@EnableAsync
public class CloudtalkApplication {

    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                            MessageListenerAdapter listenerAdapter,
                                            MessageListenerAdapter dlqListenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic("cloudtalk-reviews"));
        container.addMessageListener(dlqListenerAdapter, new PatternTopic("cloudtalk-reviews-dlq"));
        
        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapter(RedisMessageSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "onMessage");
    }
    
    @Bean
    MessageListenerAdapter dlqListenerAdapter(RedisDLQMessageSubscriber dlqSubscriber) {
        return new MessageListenerAdapter(dlqSubscriber, "onMessage");
    }
    
    @Bean
    StringRedisTemplate template(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
    
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
    
    
    public static void main(String[] args) {
        SpringApplication.run(CloudtalkApplication.class, args);
    }
	
}
