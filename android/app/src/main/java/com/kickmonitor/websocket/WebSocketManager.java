package com.kickmonitor.websocket;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.kickmonitor.model.ChatMessage;
import com.kickmonitor.model.DonationEvent;
import com.kickmonitor.model.FollowerEvent;
import com.kickmonitor.model.StreamEvent;
import com.kickmonitor.model.SubscriptionEvent;
import com.kickmonitor.model.ViewerStats;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketManager {

    public interface Listener {
        void onConnected();
        void onDisconnected();
        void onChat(ChatMessage msg);
        void onViewers(ViewerStats stats);
        void onFollower(FollowerEvent f);
        void onDonation(DonationEvent d);
        void onSubscription(SubscriptionEvent s);
        void onError(String err);
    }

    private static final String TAG = "WS";
    private final Listener listener;
    private final Gson gson = new Gson();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private OkHttpClient client;
    private WebSocket webSocket;
    private String url;
    private boolean shouldReconnect = true;

    public WebSocketManager(Listener listener) {
        this.listener = listener;
        client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();
    }

    public void connect(String baseUrl) {
        // baseUrl like http://192.168.x.x:8080
        String wsUrl = baseUrl.replace("http://", "ws://").replace("https://", "wss://");
        if (!wsUrl.endsWith("/")) wsUrl += "/";
        url = wsUrl + "stream";
        shouldReconnect = true;
        doConnect();
    }

    private void doConnect() {
        Request request = new Request.Builder().url(url).build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                handler.post(() -> listener.onConnected());
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                handle(text);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                handler.post(() -> listener.onDisconnected());
                scheduleReconnect();
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                handler.post(() -> listener.onError(t.getMessage()));
                handler.post(() -> listener.onDisconnected());
                scheduleReconnect();
            }
        });
    }

    private void scheduleReconnect() {
        if (!shouldReconnect) return;
        handler.postDelayed(this::doConnect, 4000);
    }

    private void handle(String text) {
        try {
            StreamEvent ev = gson.fromJson(text, StreamEvent.class);
            if (ev == null || ev.type == null) return;
            JsonObject payload = gson.toJsonTree(ev.payload).getAsJsonObject();
            switch (ev.type) {
                case "CHAT":
                    ChatMessage cm = gson.fromJson(payload, ChatMessage.class);
                    handler.post(() -> listener.onChat(cm));
                    break;
                case "VIEWERS":
                    ViewerStats vs = new ViewerStats();
                    vs.current = payload.get("current").getAsInt();
                    vs.max = payload.get("max").getAsInt();
                    handler.post(() -> listener.onViewers(vs));
                    break;
                case "FOLLOWER":
                    FollowerEvent fe = gson.fromJson(payload, FollowerEvent.class);
                    handler.post(() -> listener.onFollower(fe));
                    break;
                case "DONATION":
                    DonationEvent de = gson.fromJson(payload, DonationEvent.class);
                    handler.post(() -> listener.onDonation(de));
                    break;
                case "SUBSCRIPTION":
                    SubscriptionEvent se = gson.fromJson(payload, SubscriptionEvent.class);
                    handler.post(() -> listener.onSubscription(se));
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "parse error", e);
        }
    }

    public void disconnect() {
        shouldReconnect = false;
        if (webSocket != null) webSocket.close(1000, "bye");
    }
}
