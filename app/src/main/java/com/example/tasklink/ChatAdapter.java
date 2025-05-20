package com.example.tasklink;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_SENDER = 1;
    private static final int TYPE_RECEIVER = 2;

    private List<ChatMessageModel> chatList;
    private String currentUserId;

    public ChatAdapter(List<ChatMessageModel> chatList, String currentUserId) {
        this.chatList = chatList;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessageModel message = chatList.get(position);
        return message.getSenderId().equals(currentUserId) ? TYPE_SENDER : TYPE_RECEIVER;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENDER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_sender, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_receiver, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChatMessageModel message = chatList.get(position);
        String time = new SimpleDateFormat("HH:mm").format(new Date(message.getTimestamp()));

        if (holder instanceof SenderViewHolder) {
            ((SenderViewHolder) holder).message.setText(message.getMessage());
            ((SenderViewHolder) holder).timestamp.setText(time);
        } else {
            ((ReceiverViewHolder) holder).message.setText(message.getMessage());
            ((ReceiverViewHolder) holder).timestamp.setText(time);
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static class SenderViewHolder extends RecyclerView.ViewHolder {
        TextView message, timestamp;
        SenderViewHolder(View view) {
            super(view);
            message = view.findViewById(R.id.textMessageSender);
            timestamp = view.findViewById(R.id.textTimestampSender);
        }
    }

    static class ReceiverViewHolder extends RecyclerView.ViewHolder {
        TextView message, timestamp;
        ReceiverViewHolder(View view) {
            super(view);
            message = view.findViewById(R.id.textMessageReceiver);
            timestamp = view.findViewById(R.id.textTimestampReceiver);
        }
    }
}