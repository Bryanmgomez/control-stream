package com.kickmonitor.controller;

import com.kickmonitor.service.EventBroadcaster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class StreamController {

    @Autowired
    private EventBroadcaster broadcaster;

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> m = new HashMap<>();
        m.put("status", "ok");
        m.put("app", "Kick Stream Monitor");
        return m;
    }

    @GetMapping("/history/chat")
    public Object chatHistory() { return broadcaster.getChatHistory(); }

    @GetMapping("/history/followers")
    public Object followerHistory() { return broadcaster.getFollowerHistory(); }

    @GetMapping("/history/donations")
    public Object donationHistory() { return broadcaster.getDonationHistory(); }

    @GetMapping("/history/subscriptions")
    public Object subscriptionHistory() { return broadcaster.getSubscriptionHistory(); }

    @GetMapping("/viewers/max")
    public Map<String, Integer> maxViewers() {
        Map<String, Integer> m = new HashMap<>();
        m.put("max", broadcaster.getMaxViewers());
        return m;
    }
}
