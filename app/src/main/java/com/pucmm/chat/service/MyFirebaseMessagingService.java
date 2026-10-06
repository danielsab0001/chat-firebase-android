package com.pucmm.chat.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.pucmm.chat.R;
import com.pucmm.chat.repository.UserRepository;
import com.pucmm.chat.view.ChatActivity;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "chat_messages";

    @Override
    public void onNewToken(@NonNull String token) {
        new UserRepository().saveFcmToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        String senderId = data.get("senderId");
        String senderName = data.get("senderName");
        String body = data.get("body");
        if (senderId == null || senderName == null || body == null) {
            return;
        }

        if (senderId.equals(ChatActivity.currentChatUserId)) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        showNotification(senderId, senderName, body);
    }

    private void showNotification(String senderId, String senderName, String body) {
        NotificationManager manager = getSystemService(NotificationManager.class);
        createChannel(manager);

        Intent chatIntent = new Intent(this, ChatActivity.class);
        chatIntent.putExtra(ChatActivity.EXTRA_USER_ID, senderId);
        chatIntent.putExtra(ChatActivity.EXTRA_USER_NAME, senderName);

        PendingIntent pendingIntent = TaskStackBuilder.create(this)
                .addNextIntentWithParentStack(chatIntent)
                .getPendingIntent(senderId.hashCode(),
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentTitle(senderName)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();

        manager.notify(senderId.hashCode(), notification);
    }

    // Desde Android 8 toda notificación necesita un canal
    private void createChannel(NotificationManager manager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }
    }
}