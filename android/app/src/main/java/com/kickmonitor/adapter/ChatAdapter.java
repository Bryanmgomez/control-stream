package com.kickmonitor.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kickmonitor.R;
import com.kickmonitor.model.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.VH> {

    private final List<ChatMessage> items = new ArrayList<>();

    public void add(ChatMessage m) {
        items.add(m);
        notifyItemInserted(items.size() - 1);
    }

    public void clear() { items.clear(); notifyDataSetChanged(); }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ChatMessage m = items.get(position);
        holder.user.setText(m.username != null ? m.username : "anon");
        holder.msg.setText(m.content != null ? m.content : "");
        try {
            if (m.color != null && !m.color.isEmpty()) holder.user.setTextColor(android.graphics.Color.parseColor(m.color));
        } catch (Exception ignored) {}
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView user, msg;
        VH(View v) {
            super(v);
            user = v.findViewById(R.id.userName);
            msg = v.findViewById(R.id.messageText);
        }
    }
}
