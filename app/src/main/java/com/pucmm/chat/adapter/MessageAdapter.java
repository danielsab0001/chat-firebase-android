package com.pucmm.chat.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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

        if (holder instanceof SentViewHolder) {
            ItemMessageSentBinding b = ((SentViewHolder) holder).binding;
            bindMessage(message, b.tvSender, b.tvMessage, b.ivImage, b.tvTime);
        } else {
            ItemMessageReceivedBinding b = ((ReceivedViewHolder) holder).binding;
            bindMessage(message, b.tvSender, b.tvMessage, b.ivImage, b.tvTime);
        }
    }

    private void bindMessage(Message message, TextView tvSender, TextView tvMessage,
                             ImageView ivImage, TextView tvTime) {
        tvSender.setText(message.getSenderName());
        tvTime.setText(formatTime(message.getTimestamp()));

        boolean hasText = message.getText() != null && !message.getText().isEmpty();
        tvMessage.setText(message.getText());
        tvMessage.setVisibility(hasText ? View.VISIBLE : View.GONE);

        boolean hasImage = message.getImageUrl() != null && !message.getImageUrl().isEmpty();
        if (hasImage) {
            ivImage.setVisibility(View.VISIBLE);
            Glide.with(ivImage)
                    .load(message.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .centerCrop()
                    .into(ivImage);
        } else {
            // Las filas se reciclan: hay que cancelar la carga anterior y ocultar la imagen
            Glide.with(ivImage).clear(ivImage);
            ivImage.setVisibility(View.GONE);
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