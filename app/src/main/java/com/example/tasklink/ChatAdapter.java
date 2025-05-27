package com.example.tasklink;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
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
        View view;
        if (viewType == TYPE_SENDER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_sender, parent, false);
            return new SenderViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_receiver, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChatMessageModel message = chatList.get(position);
        String time = new SimpleDateFormat("HH:mm").format(new Date(message.getTimestamp()));

        if (holder instanceof SenderViewHolder) {
            bindMessage((SenderViewHolder) holder, message, time);
        } else {
            bindMessage((ReceiverViewHolder) holder, message, time);
        }
    }

    private void bindMessage(BaseViewHolder holder, ChatMessageModel message, String time) {
        if (message.getFileUrl() != null) {
            holder.textView.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.VISIBLE);
            Glide.with(holder.imageView.getContext()).load(message.getFileUrl()).into(holder.imageView);
            holder.imageView.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(message.getFileUrl()));
                holder.imageView.getContext().startActivity(intent);
            });
        } else {
            holder.textView.setVisibility(View.VISIBLE);
            holder.textView.setText(message.getMessage());
            holder.imageView.setVisibility(View.GONE);
        }
        holder.timestamp.setText(time);
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    abstract class BaseViewHolder extends RecyclerView.ViewHolder {
        TextView textView, timestamp;
        ImageView imageView;

        BaseViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.textMessage);
            timestamp = view.findViewById(R.id.textTimestamp);
            imageView = view.findViewById(R.id.imageAttachment);
        }
    }

    class SenderViewHolder extends BaseViewHolder {
        SenderViewHolder(View view) { super(view); }
    }

    class ReceiverViewHolder extends BaseViewHolder {
        ReceiverViewHolder(View view) { super(view); }
    }
}
