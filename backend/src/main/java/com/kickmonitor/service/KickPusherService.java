package com.kickmonitor.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kickmonitor.model.ChatMessage;
import com.kickmonitor.model.DonationEvent;
import com.kickmonitor.model.SubscriptionEvent;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class KickPusherService {

    @Value("${kick.channel}")
    private String channel;

    @Value("${kick.pusher.app-key}")
    private String appKey;

    @Value("${kick.pusher.cluster}")
    private String cluster;

    @Value("${kick.api.base-url}")
    private String baseUrl;

    @Autowired
    private EventBroadcaster broadcaster;

    private final ObjectMapper mapper = new ObjectMapper();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private WebSocketClient client;
    private String chatroomId;
    private String channelId;
    private boolean connected = false;

    @PostConstruct
    public void init() {
        scheduler.schedule(this::connect, 2, TimeUnit.SECONDS);
    }

    private void fetchIds() {
        try {
            java.net.URL url = new java.net.URL(baseUrl + "/channels/" + channel);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setRequestProperty("Accept", "application/json");
            java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();
            JsonNode root = mapper.readTree(sb.toString());
            chatroomId = root.path("chatroom").path("id").asText();
            channelId = root.path("id").asText();
        } catch (Exception e) {
            // retry later
        }
    }

    private void connect() {
        if (chatroomId == null || channelId == null) fetchIds();
        String wsUrl = "wss://ws-" + cluster + ".pusher.com/app/" + appKey
                + "?protocol=7&client=js&version=7.6.0&flash=false";
        try {
            client = new WebSocketClient(new URI(wsUrl)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    connected = true;
                    subscribeChannels();
                }

                @Override
                public void onMessage(String message) {
                    handleMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    connected = false;
                    scheduler.schedule(KickPusherService.this::connect, 5, TimeUnit.SECONDS);
                }

                @Override
                public void onError(Exception ex) {
                    // ignore
                }
            };
            client.connect();
        } catch (Exception e) {
            scheduler.schedule(this::connect, 5, TimeUnit.SECONDS);
        }
    }

    private void subscribeChannels() {
        if (chatroomId != null && !chatroomId.isEmpty()) {
            sendSubscribe("chatrooms." + chatroomId + ".v2");
        }
        if (channelId != null && !channelId.isEmpty()) {
            sendSubscribe("channel." + channelId);
        }
    }

    private void sendSubscribe(String channelName) {
        try {
            String msg = "{\"event\":\"pusher:subscribe\",\"data\":{\"auth\":\"\",\"channel\":\"" + channelName + "\"}}";
            client.send(msg);
        } catch (Exception ignored) {}
    }

    private void handleMessage(String raw) {
        try {
            JsonNode root = mapper.readTree(raw);
            String event = root.path("event").asText("");
            String channelName = root.path("channel").asText("");
            String dataStr = root.path("data").asText("");
            if (event.equals("pusher:connection_established") || event.equals("pusher_internal:subscription_succeeded")) {
                return;
            }
            if (dataStr.isEmpty()) return;
            JsonNode data = mapper.readTree(dataStr);

            if (event.contains("ChatMessage")) {
                JsonNode sender = data.path("sender");
                ChatMessage cm = new ChatMessage();
                cm.setId(data.path("id").asText());
                cm.setUsername(sender.path("username").asText(data.path("username").asText("")));
                cm.setContent(data.path("content").asText(""));
                cm.setColor(sender.path("identity").path("color").asText("#FFFFFF"));
                cm.setSubscriber(sender.path("is_subscriber").asBoolean(false) || sender.path("subscriber").asBoolean(false));
                cm.setModerator(sender.path("is_moderator").asBoolean(false));
                broadcaster.broadcastChat(cm);
            } else if (event.contains("Subscription")) {
                String user = data.path("username").asText(data.path("subscriber").path("username").asText(""));
                SubscriptionEvent se = new SubscriptionEvent(user, "NEW_SUB");
                se.setMonths(data.path("months").asInt(1));
                broadcaster.broadcastSubscription(se);
            } else if (event.contains("GiftedSubscriptions") || event.contains("GiftedSubs")) {
                String gifter = data.path("gifter").path("username").asText(data.path("username").asText(""));
                int qty = data.path("quantity").asInt(data.path("count").asInt(1));
                SubscriptionEvent se = new SubscriptionEvent(gifter, "GIFTED");
                se.setQuantity(qty);
                broadcaster.broadcastSubscription(se);
            } else if (event.contains("Kicks") || event.contains("Tip") || event.contains("Donation")) {
                String user = data.path("sender").path("username").asText(data.path("username").asText(""));
                int amount = data.path("amount").asInt(data.path("kicks").asInt(0));
                if (amount == 0) amount = data.path("value").asInt(0);
                DonationEvent de = new DonationEvent(user, amount);
                de.setMessage(data.path("message").asText(""));
                broadcaster.broadcastDonation(de);
            }
        } catch (Exception ignored) {}
    }
}
