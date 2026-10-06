package com.pucmm.chat.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pucmm.chat.databinding.ItemMessageReceivedBinding;
import com.pucmm.chat.databinding.ItemMessageSentBinding;
import com.pucmm.chat.model.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    private final List<Message> messages = new ArrayList<>();
    private final String currentUserId;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public MessageAdapter(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setMessages(List<Message> newMessages) {
        messages.clear();
        messages.addAll(newMessages);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        boolean isMine = currentUserId.equals(messages.get(position).getSenderId());
        return isMine ? TYPE_SENT : TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_SENT) {
            return new SentViewHolder(ItemMessageSentBinding.inflate(inflater, parent, false));
        }
        return new ReceivedViewHolder(ItemMessageReceivedBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        String time = formatTime(message.getTimestamp());

        if (holder instanceof SentViewHolder) {
            ItemMessageSentBinding b = ((SentViewHolder) holder).binding;
            b.tvSender.setText(message.getSenderName());
            b.tvMessage.setText(message.getText());
            b.tvTime.setText(time);
        } else {
            ItemMessageReceivedBinding b = ((ReceivedViewHolder) holder).binding;
            b.tvSender.setText(message.getSenderName());
            b.tvMessage.setText(message.getText());
            b.tvTime.setText(time);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private String formatTime(Date date) {
        return date == null ? "" : dateFormat.format(date);
    }

    static class SentViewHolder extends RecyclerView.ViewHolder {
        final ItemMessageSentBinding binding;

        SentViewHolder(ItemMessageSentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        final ItemMessageReceivedBinding binding;

        ReceivedViewHolder(ItemMessageReceivedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}