package com.kickmonitor.ui;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kickmonitor.R;
import com.kickmonitor.adapter.ChatAdapter;
import com.kickmonitor.adapter.EventAdapter;
import com.kickmonitor.model.ChatMessage;
import com.kickmonitor.model.DonationEvent;
import com.kickmonitor.model.FollowerEvent;
import com.kickmonitor.model.SubscriptionEvent;
import com.kickmonitor.model.ViewerStats;
import com.kickmonitor.websocket.WebSocketManager;

public class MainActivity extends AppCompatActivity implements WebSocketManager.Listener {

    private TextView statusText, viewersCount, viewersMax;
    private RecyclerView chatRecycler, followersRecycler, donationsRecycler, subsRecycler;
    private ChatAdapter chatAdapter;
    private EventAdapter followersAdapter, donationsAdapter, subsAdapter;
    private WebSocketManager ws;
    private boolean connected = false;
    private FloatingActionButton fab;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        viewersCount = findViewById(R.id.viewersCount);
        viewersMax = findViewById(R.id.viewersMax);
        chatRecycler = findViewById(R.id.chatRecycler);
        followersRecycler = findViewById(R.id.followersRecycler);
        donationsRecycler = findViewById(R.id.donationsRecycler);
        subsRecycler = findViewById(R.id.subsRecycler);
        fab = findViewById(R.id.connectFab);

        chatAdapter = new ChatAdapter();
        followersAdapter = new EventAdapter();
        donationsAdapter = new EventAdapter();
        subsAdapter = new EventAdapter();

        chatRecycler.setLayoutManager(new LinearLayoutManager(this));
        chatRecycler.setAdapter(chatAdapter);
        followersRecycler.setLayoutManager(new LinearLayoutManager(this));
        followersRecycler.setAdapter(followersAdapter);
        donationsRecycler.setLayoutManager(new LinearLayoutManager(this));
        donationsRecycler.setAdapter(donationsAdapter);
        subsRecycler.setLayoutManager(new LinearLayoutManager(this));
        subsRecycler.setAdapter(subsAdapter);

        ws = new WebSocketManager(this);
        prefs = getSharedPreferences("kms", MODE_PRIVATE);

        fab.setOnClickListener(v -> {
            if (connected) {
                ws.disconnect();
            } else {
                promptConnect();
            }
        });
    }

    private void promptConnect() {
        String saved = prefs.getString("url", "http://10.0.2.2:8080");
        EditText input = new EditText(this);
        input.setText(saved);
        new AlertDialog.Builder(this)
                .setTitle("URL del backend")
                .setView(input)
                .setPositiveButton("Conectar", (d, w) -> {
                    String url = input.getText().toString().trim();
                    prefs.edit().putString("url", url).apply();
                    ws.connect(url);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onConnected() {
        connected = true;
        statusText.setText("EN VIVO");
        statusText.setTextColor(getColorCompat(R.color.neon_green));
        fab.setImageResource(android.R.drawable.ic_media_pause);
    }

    @Override
    public void onDisconnected() {
        connected = false;
        statusText.setText("DESCONECTADO");
        statusText.setTextColor(getColorCompat(R.color.accent_red));
        fab.setImageResource(android.R.drawable.ic_media_play);
    }

    @Override
    public void onChat(ChatMessage msg) {
        chatAdapter.add(msg);
        chatRecycler.scrollToPosition(chatAdapter.getItemCount() - 1);
    }

    @Override
    public void onViewers(ViewerStats stats) {
        viewersCount.setText(String.valueOf(stats.current));
        if (stats.max > 0) viewersMax.setText(String.valueOf(stats.max));
    }

    @Override
    public void onFollower(FollowerEvent f) {
        followersAdapter.add("Nuevo seguidor: " + (f.username != null ? f.username : "anon"));
        Toast.makeText(this, "Nuevo seguidor: " + f.username, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDonation(DonationEvent d) {
        String msg = (d.username != null ? d.username : "anon") + " envió " + d.amount + " " + (d.currency != null ? d.currency : "KICKs");
        if (d.message != null && !d.message.isEmpty()) msg += " - " + d.message;
        donationsAdapter.add(msg);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSubscription(SubscriptionEvent s) {
        String label = s.type != null ? s.type : "SUB";
        String msg = (s.username != null ? s.username : "anon") + " - " + label;
        if (s.quantity > 0) msg += " x" + s.quantity;
        if (s.months > 0) msg += " (" + s.months + " meses)";
        subsAdapter.add(msg);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(String err) {
        if (err != null) Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
    }

    private int getColorCompat(int id) {
        return androidx.core.content.ContextCompat.getColor(this, id);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ws.disconnect();
    }
}
