package com.example.cloudtalk.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Async
    public void notifyExternalService(String message) {
        // Simulate async processing (no real service call)
        System.out.println("Simulated notification: " + message);
    }
}
