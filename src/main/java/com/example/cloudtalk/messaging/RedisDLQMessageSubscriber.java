package com.example.cloudtalk.messaging;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RedisDLQMessageSubscriber {

    
    public void onMessage(String message) {
        // Handle the message from the DLQ
        log.info("Received message from DLQ: {} ", message);
        // Implement your DLQ message processing logic here
    }
}
