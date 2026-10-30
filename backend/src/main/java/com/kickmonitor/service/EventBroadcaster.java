package com.kickmonitor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kickmonitor.model.StreamEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class EventBroadcaster {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private StreamWebSocketHandler rawHandler;

    private final ObjectMapper mapper = new ObjectMapper();

    private final List<Object> followerHistory = new CopyOnWriteArrayList<>();
    private final List<Object> donationHistory = new CopyOnWriteArrayList<>();
    private final List<Object> subscriptionHistory = new CopyOnWriteArrayList<>();
    private final List<Object> chatHistory = new CopyOnWriteArrayList<>();
    private volatile int maxViewers = 0;

    public void broadcast(String type, Object payload) {
        StreamEvent event = new StreamEvent(type, payload);
        messagingTemplate.convertAndSend("/topic/stream", event);
        try {
            rawHandler.broadcast(mapper.writeValueAsString(event));
        } catch (Exception ignored) {}
    }

    public void broadcastChat(Object chat) {
        chatHistory.add(chat);
        if (chatHistory.size() > 200) chatHistory.remove(0);
        broadcast("CHAT", chat);
    }

    public void broadcastViewers(int current) {
        if (current > maxViewers) maxViewers = current;
        broadcast("VIEWERS", new ViewerPayload(current, maxViewers));
        // nested class below
    }

    public void broadcastFollower(Object f) {
        followerHistory.add(f);
        broadcast("FOLLOWER", f);
    }

    public void broadcastDonation(Object d) {
        donationHistory.add(d);
        broadcast("DONATION", d);
    }

    public void broadcastSubscription(Object s) {
        subscriptionHistory.add(s);
        broadcast("SUBSCRIPTION", s);
    }

    public List<Object> getFollowerHistory() { return new ArrayList<>(followerHistory); }
    public List<Object> getDonationHistory() { return new ArrayList<>(donationHistory); }
    public List<Object> getSubscriptionHistory() { return new ArrayList<>(subscriptionHistory); }
    public List<Object> getChatHistory() { return new ArrayList<>(chatHistory); }
    public int getMaxViewers() { return maxViewers; }

    public static class ViewerPayload {
        public int current;
        public int max;
        public ViewerPayload(int c, int m) { this.current = c; this.max = m; }
    }
}
