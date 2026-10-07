package com.pucmm.chat.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.pucmm.chat.R;
import com.pucmm.chat.databinding.ItemUserBinding;
import com.pucmm.chat.model.ChatItem;
import com.pucmm.chat.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    private static final int MAX_UNREAD_SHOWN = 99;

    private final List<ChatItem> items = new ArrayList<>();
    private final OnUserClickListener listener;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());

    public UserAdapter(OnUserClickListener listener) {
        this.listener = listener;
    }

    public void setUsers(List<ChatItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserBinding binding = ItemUserBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        ChatItem item = items.get(position);
        User user = item.getUser();
        ItemUserBinding b = holder.binding;
        Context context = holder.itemView.getContext();

        String name = user.getName();
        b.tvAvatar.setText(name == null || name.isEmpty()
                ? "?" : String.valueOf(Character.toUpperCase(name.charAt(0))));
        b.tvUserName.setText(name);

        int grey = ContextCompat.getColor(context, R.color.on_surface_variant);

        if (item.getLastMessage() == null) {
            b.tvLastMessage.setText(user.getEmail());
            b.tvLastMessage.setTextColor(grey);
            b.tvTime.setVisibility(View.GONE);
            b.tvUnread.setVisibility(View.GONE);
        } else {
            String preview = item.isLastMessageFromMe()
                    ? context.getString(R.string.last_message_mine, item.getLastMessage())
                    : item.getLastMessage();
            b.tvLastMessage.setText(preview);

            b.tvTime.setText(formatTime(context, item.getLastMessageTime()));
            b.tvTime.setVisibility(View.VISIBLE);

            boolean hasUnread = item.getUnreadCount() > 0;
            b.tvUnread.setVisibility(hasUnread ? View.VISIBLE : View.GONE);
            b.tvUnread.setText(item.getUnreadCount() > MAX_UNREAD_SHOWN
                    ? context.getString(R.string.unread_overflow)
                    : String.valueOf(item.getUnreadCount()));
            b.tvTime.setTextColor(hasUnread
                    ? ContextCompat.getColor(context, R.color.primary) : grey);
            b.tvLastMessage.setTextColor(hasUnread
                    ? ContextCompat.getColor(context, R.color.on_surface) : grey);
        }

        holder.itemView.setOnClickListener(v -> listener.onUserClick(user));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String formatTime(Context context, Date date) {
        if (date == null) {
            return "";
        }
        if (DateUtils.isToday(date.getTime())) {
            return timeFormat.format(date);
        }
        if (DateUtils.isToday(date.getTime() + DateUtils.DAY_IN_MILLIS)) {
            return context.getString(R.string.yesterday);
        }
        return dateFormat.format(date);
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        final ItemUserBinding binding;

        UserViewHolder(ItemUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}