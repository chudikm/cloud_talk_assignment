package com.example.cloudtalk.controller;

import org.springframework.web.bind.annotation.*;

import com.example.cloudtalk.messaging.RedisMessagePublisher;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class MessageController {

    private final RedisMessagePublisher publisher;

    
    @PostMapping("/publish")
    public String publishMessage(@RequestParam String message) {
        publisher.publish("cloudtalk-channel", message);
        return "Message published: " + message;
    }
}