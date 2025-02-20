package com.example.cloudtalk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import com.example.cloudtalk.messaging.RedisDLQMessageSubscriber;
import com.example.cloudtalk.messaging.RedisMessageSubscriber;

@SpringBootApplication//(exclude = {DataSourceAutoConfiguration.class})
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
    
    
    public static void main(String[] args) {
        SpringApplication.run(CloudtalkApplication.class, args);
    }
	
}
