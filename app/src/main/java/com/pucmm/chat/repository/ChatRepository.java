package com.pucmm.chat.repository;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.pucmm.chat.model.Message;
import com.pucmm.chat.util.Callback;

import java.util.ArrayList;
import java.util.List;

public class ChatRepository {

    private static final String TAG = "ChatRepository";
    private static final String CHATS_COLLECTION = "chats";
    private static final String MESSAGES_COLLECTION = "messages";

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration registration;

    public String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public static String buildChatId(String uidA, String uidB) {
        if (uidA.compareTo(uidB) < 0) {
            return uidA + "_" + uidB;
        }
        return uidB + "_" + uidA;
    }

    private CollectionReference messagesOf(String chatId) {
        return db.collection(CHATS_COLLECTION).document(chatId).collection(MESSAGES_COLLECTION);
    }

    public void listenMessages(String chatId, Callback<List<Message>> callback) {
        stopListening();
        registration = messagesOf(chatId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) {
                        Log.e(TAG, "Error al escuchar mensajes", e);
                        callback.onError("No se pudieron cargar los mensajes");
                        return;
                    }

                    List<Message> messages = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {

                        Message message = doc.toObject(Message.class,
                                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE);
                        if (message != null) {
                            messages.add(message);
                        }
                    }
                    callback.onSuccess(messages);
                });
    }

    public void sendMessage(String chatId, String text, Callback<Void> callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onError("No hay una sesión activa");
            return;
        }

        String senderName = user.getDisplayName();
        if (senderName == null || senderName.isEmpty()) {
            senderName = "Usuario";
        }

        Message message = new Message(user.getUid(), senderName, text);
        messagesOf(chatId).add(message)
                .addOnSuccessListener(ref -> callback.onSuccess(null))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al enviar mensaje", e);
                    callback.onError("No se pudo enviar el mensaje");
                });
    }

    public void stopListening() {
        if (registration != null) {
            registration.remove();
            registration = null;
        }
    }
}