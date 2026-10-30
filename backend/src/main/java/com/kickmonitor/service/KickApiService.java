package com.kickmonitor.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Set;

@Service
public class KickApiService {

    @Value("${kick.channel}")
    private String channel;

    @Value("${kick.api.base-url}")
    private String baseUrl;

    @Autowired
    private EventBroadcaster broadcaster;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    private final Set<String> knownFollowers = new HashSet<>();
    private boolean followersInitialized = false;
    private int lastViewers = 0;

    private HttpHeaders headers() {
        HttpHeaders h = new HttpHeaders();
        h.set("User-Agent", "Mozilla/5.0 (KickStreamMonitor)");
        h.set("Accept", "application/json");
        return h;
    }

    @Scheduled(fixedDelayString = "${kick.polling.viewers-interval-ms:5000}")
    public void pollViewers() {
        try {
            String url = baseUrl + "/channels/" + channel;
            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers()), String.class);
            JsonNode root = mapper.readTree(resp.getBody());
            JsonNode chatroom = root.path("chatroom");
            int viewers = root.path("viewers_count").asInt(0);
            if (viewers == 0 && chatroom.has("viewers_count")) viewers = chatroom.path("viewers_count").asInt(0);
            if (viewers != lastViewers) {
                lastViewers = viewers;
                broadcaster.broadcastViewers(viewers);
            } else {
                // still broadcast periodically? broadcast every poll to keep fresh
                broadcaster.broadcastViewers(viewers);
            }
        } catch (Exception e) {
            // ignore transient
        }
    }

    @Scheduled(fixedDelayString = "${kick.polling.followers-interval-ms:15000}")
    public void pollFollowers() {
        try {
            String url = baseUrl + "/channels/" + channel + "/followers?cursor=";
            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers()), String.class);
            JsonNode root = mapper.readTree(resp.getBody());
            JsonNode data = root.path("data");
            if (!data.isArray()) data = root.path("followers");
            if (data.isArray()) {
                Set<String> current = new HashSet<>();
                for (JsonNode f : data) {
                    String name = f.path("username").asText(f.path("user").path("username").asText(""));
                    if (name.isEmpty()) continue;
                    current.add(name);
                    if (followersInitialized && !knownFollowers.contains(name)) {
                        broadcaster.broadcastFollower(new com.kickmonitor.model.FollowerEvent(name));
                    }
                }
                knownFollowers.clear();
                knownFollowers.addAll(current);
                followersInitialized = true;
            }
        } catch (Exception e) {
            // ignore
        }
    }

    public String getChannelSlug() { return channel; }
}
